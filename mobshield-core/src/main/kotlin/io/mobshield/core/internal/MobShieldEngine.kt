/*
 * Copyright 2025 MobShield Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mobshield.core.internal

import io.mobshield.core.DetectionModule
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.MobShieldListener
import io.mobshield.core.MobShieldState
import io.mobshield.core.RiskLevel
import io.mobshield.core.Severity
import io.mobshield.core.Signal
import io.mobshield.core.SignalAggregator
import io.mobshield.core.TerminationPolicy
import io.mobshield.core.ThreatEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.coroutineContext

internal class MobShieldEngine(
    private val config: MobShieldConfig,
    private val listener: MobShieldListener,
    private val resolveModules: () -> List<DetectionModule>,
    private val scope: CoroutineScope,
    private val signalSetVersion: String,
    periodicIntervalMsOverride: Long? = null,
    private val terminate: () -> Unit = defaultTerminate,
    private val selfCheck: () -> Int = defaultSelfCheck,
) {
    private val stateRef = AtomicReference(idleState())
    private val lastEventsRef = AtomicReference<List<ThreatEvent>>(emptyList())
    private var scanJob: Job? = null

    // Rescan cadence in milliseconds; null runs a single scan wave (spec default).
    private val periodicIntervalMs: Long? =
        periodicIntervalMsOverride ?: config.periodicIntervalSec?.let { it.toLong() * MILLIS_PER_SECOND }

    fun start() {
        scanJob?.cancel()
        scanJob =
            scope.launch {
                runScanLoop()
            }
    }

    /**
     * Runs one scan wave, then repeats every [periodicIntervalMs] until cancelled. When no interval
     * is configured, runs exactly one wave. Terminates the process (and ends the loop) once the
     * configured [TerminationPolicy] is satisfied.
     */
    private suspend fun runScanLoop() {
        while (coroutineContext.isActive) {
            val events = runScanWave()
            if (shouldTerminate(events, config.detectOnly, config.terminationPolicy)) {
                terminate()
                return
            }
            val intervalMs = periodicIntervalMs ?: return
            delay(intervalMs)
        }
    }

    fun stop() {
        scanJob?.cancel()
        scanJob = null
        stateRef.set(idleState())
        lastEventsRef.set(emptyList())
    }

    fun getState(): MobShieldState = stateRef.get()

    fun getLastEvents(): List<ThreatEvent> = lastEventsRef.get()

    private suspend fun runScanWave(): List<ThreatEvent> {
        val modules = resolveModules()
        val moduleSignals =
            if (modules.isEmpty()) {
                emptyList()
            } else {
                coroutineScope {
                    modules
                        .map { module ->
                            async {
                                runCatching { module.scan() }.getOrElse { emptyList() }
                            }
                        }.awaitAll()
                        .flatten()
                }
            }

        // Native core integrity runs every wave, independent of registered modules.
        val signals = moduleSignals + listOfNotNull(makeSelfCheckSignal())

        val aggregator = SignalAggregator(config)
        val events = aggregator.aggregate(signals)
        for (event in events) {
            listener.onThreat(event)
        }
        listener.onAllChecksFinished(events)
        stateRef.set(buildState(events, running = true))
        lastEventsRef.set(events)
        return events
    }

    /**
     * Emits an integrity signal when the native core self-check reports an unhealthy (zero) result,
     * which indicates the native core was zeroed, swapped, or otherwise tampered with. Per the
     * documented native contract, a healthy core returns nonzero.
     */
    private fun makeSelfCheckSignal(): Signal? {
        if (selfCheck() != 0) return null
        return Signal(
            name = SELF_CHECK_SIGNAL,
            weight = SELF_CHECK_WEIGHT,
            confidence = SELF_CHECK_CONFIDENCE,
            evidence = mapOf("reason" to "native_self_check_failed"),
        )
    }

    private fun buildState(
        events: List<ThreatEvent>,
        running: Boolean,
    ): MobShieldState {
        val active = events.map { it.type }.distinct()
        val maxSeverity = events.maxOfOrNull { severityRank(it.severity) } ?: 0
        val risk =
            when (maxSeverity) {
                0 -> RiskLevel.NONE
                1, 2 -> RiskLevel.LOW
                3 -> RiskLevel.MEDIUM
                else -> RiskLevel.HIGH
            }
        return MobShieldState(
            riskLevel = risk,
            activeThreats = active,
            lastScanMs = System.currentTimeMillis(),
            signalSetVersion = signalSetVersion,
            running = running,
        )
    }

    private fun severityRank(severity: Severity): Int =
        when (severity) {
            Severity.INFO -> 0
            Severity.LOW -> 1
            Severity.MEDIUM -> 2
            Severity.HIGH -> 3
            Severity.CRITICAL -> 4
        }

    private fun idleState(): MobShieldState =
        MobShieldState(
            riskLevel = RiskLevel.NONE,
            activeThreats = emptyList(),
            lastScanMs = 0L,
            signalSetVersion = signalSetVersion,
            running = false,
        )

    companion object {
        const val SIGNAL_SET_VERSION = "signals-2026.05.0"

        private const val MILLIS_PER_SECOND = 1000L

        /** Signal name for the native core integrity probe; maps to APP_INTEGRITY. */
        internal const val SELF_CHECK_SIGNAL = "android.integrity.native_self_check"
        private const val SELF_CHECK_WEIGHT = 90
        private const val SELF_CHECK_CONFIDENCE = 95

        /** Default process-exit action; killing the process is the Android RASP idiom. */
        internal val defaultTerminate: () -> Unit = {
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        /** Native core integrity probe; a healthy core returns nonzero. */
        internal val defaultSelfCheck: () -> Int = { NativeBridge.selfCheck() }

        /**
         * Decides whether the current scan results warrant process termination.
         *
         * - [TerminationPolicy.NONE] (and any `detectOnly` config): never terminates.
         * - [TerminationPolicy.EXIT_ON_BYPASS]: terminates when any threat reaches [Severity.HIGH]
         *   or above — a confirmed compromise of the protected environment.
         * - [TerminationPolicy.EXIT_ON_CRITICAL]: terminates only when a threat reaches
         *   [Severity.CRITICAL].
         */
        internal fun shouldTerminate(
            events: List<ThreatEvent>,
            detectOnly: Boolean,
            policy: TerminationPolicy,
        ): Boolean {
            if (detectOnly) return false
            return when (policy) {
                TerminationPolicy.NONE -> false
                TerminationPolicy.EXIT_ON_BYPASS -> events.any { it.severity >= Severity.HIGH }
                TerminationPolicy.EXIT_ON_CRITICAL -> events.any { it.severity == Severity.CRITICAL }
            }
        }
    }
}

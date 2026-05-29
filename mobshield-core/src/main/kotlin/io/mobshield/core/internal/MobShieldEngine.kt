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
import io.mobshield.core.SignalAggregator
import io.mobshield.core.ThreatEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

internal class MobShieldEngine(
    private val config: MobShieldConfig,
    private val listener: MobShieldListener,
    private val resolveModules: () -> List<DetectionModule>,
    private val scope: CoroutineScope,
    private val signalSetVersion: String,
) {
    private val stateRef = AtomicReference(idleState())
    private var scanJob: Job? = null

    fun start() {
        scanJob?.cancel()
        scanJob =
            scope.launch {
                runScanWave()
            }
    }

    fun stop() {
        scanJob?.cancel()
        scanJob = null
        stateRef.set(idleState())
    }

    fun getState(): MobShieldState = stateRef.get()

    private suspend fun runScanWave() {
        val modules = resolveModules()
        if (modules.isEmpty()) {
            val empty = emptyList<ThreatEvent>()
            listener.onAllChecksFinished(empty)
            stateRef.set(buildState(empty, running = true))
            return
        }

        val signals =
            coroutineScope {
                modules
                    .map { module ->
                        async {
                            runCatching { module.scan() }.getOrElse { emptyList() }
                        }
                    }.awaitAll()
                    .flatten()
            }

        val aggregator = SignalAggregator(config)
        val events = aggregator.aggregate(signals)
        for (event in events) {
            listener.onThreat(event)
        }
        listener.onAllChecksFinished(events)
        stateRef.set(buildState(events, running = true))
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
    }
}

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
package io.mobshield.core

import io.mobshield.core.internal.MobShieldEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MobShieldEngineIntegrationTest {
    @After
    fun tearDown() {
        MobShield.resetForTests()
    }

    @Test
    fun engine_runsModuleScan_andDeliversCallbacks() =
        runTest {
            val mockModule =
                object : DetectionModule {
                    override val name: String = "mock-root"
                    override val criticality: Int = 10

                    override suspend fun scan(): List<Signal> =
                        listOf(
                            Signal(
                                name = "android.root.mock",
                                weight = 90,
                                confidence = 100,
                            ),
                        )
                }

            ModuleRegistry.register(mockModule)
            val listener = RecordingListener()
            val engine =
                MobShieldEngine(
                    config = MobShieldConfig(),
                    listener = listener,
                    resolveModules = { ModuleRegistry.getAll() },
                    scope = this,
                    signalSetVersion = MobShield.SIGNAL_SET_VERSION,
                )

            engine.start()
            advanceUntilIdle()

            assertEquals(1, listener.threats.size)
            assertEquals(ThreatType.PRIVILEGED_ACCESS, listener.threats[0].type)
            assertEquals(1, listener.finished.size)
            assertTrue(engine.getState().running)
            assertTrue(engine.getState().activeThreats.contains(ThreatType.PRIVILEGED_ACCESS))

            engine.stop()
        }

    @Test
    fun engine_withoutInterval_runsSingleWave() =
        runTest {
            ModuleRegistry.register(rootModule())
            val listener = RecordingListener()
            val engine = makeEngine(this, MobShieldConfig(), listener)

            engine.start()
            advanceUntilIdle()

            assertEquals(1, listener.finished.size)
            engine.stop()
        }

    @Test
    fun engine_periodicInterval_rescansUntilStopped() =
        runTest {
            ModuleRegistry.register(rootModule())
            val listener = RecordingListener()
            val engine = makeEngine(this, MobShieldConfig(), listener, periodicIntervalMsOverride = 100L)

            engine.start()
            advanceTimeBy(350)
            runCurrent()
            val waves = listener.finished.size
            assertTrue("periodic interval should trigger repeated waves, got $waves", waves >= 2)

            engine.stop()
            advanceTimeBy(300)
            runCurrent()
            assertEquals(waves, listener.finished.size)
        }

    @Test
    fun shouldTerminate_matrix() {
        val critical = listOf(event(Severity.CRITICAL))
        val high = listOf(event(Severity.HIGH))
        val medium = listOf(event(Severity.MEDIUM))

        assertFalse(MobShieldEngine.shouldTerminate(critical, detectOnly = false, TerminationPolicy.NONE))
        // detectOnly overrides any policy.
        assertFalse(MobShieldEngine.shouldTerminate(critical, detectOnly = true, TerminationPolicy.EXIT_ON_CRITICAL))
        // EXIT_ON_CRITICAL fires only at critical.
        assertTrue(MobShieldEngine.shouldTerminate(critical, detectOnly = false, TerminationPolicy.EXIT_ON_CRITICAL))
        assertFalse(MobShieldEngine.shouldTerminate(high, detectOnly = false, TerminationPolicy.EXIT_ON_CRITICAL))
        // EXIT_ON_BYPASS fires at high or above.
        assertTrue(MobShieldEngine.shouldTerminate(high, detectOnly = false, TerminationPolicy.EXIT_ON_BYPASS))
        assertTrue(MobShieldEngine.shouldTerminate(critical, detectOnly = false, TerminationPolicy.EXIT_ON_BYPASS))
        assertFalse(MobShieldEngine.shouldTerminate(medium, detectOnly = false, TerminationPolicy.EXIT_ON_BYPASS))
        assertFalse(MobShieldEngine.shouldTerminate(emptyList(), detectOnly = false, TerminationPolicy.EXIT_ON_BYPASS))
    }

    @Test
    fun engine_exitOnCritical_terminatesOnCriticalThreat() =
        runTest {
            ModuleRegistry.register(rootModule(weight = 90, confidence = 100))
            val config = MobShieldConfig(detectOnly = false, terminationPolicy = TerminationPolicy.EXIT_ON_CRITICAL)
            var terminated = 0
            val engine = makeEngine(this, config, RecordingListener(), terminate = { terminated++ })

            engine.start()
            advanceUntilIdle()

            assertEquals(1, terminated)
            engine.stop()
        }

    @Test
    fun engine_detectOnly_neverTerminates() =
        runTest {
            ModuleRegistry.register(rootModule(weight = 90, confidence = 100))
            var terminated = 0
            val engine = makeEngine(this, MobShieldConfig(), RecordingListener(), terminate = { terminated++ })

            engine.start()
            advanceUntilIdle()

            assertEquals(0, terminated)
            engine.stop()
        }

    private fun makeEngine(
        scope: CoroutineScope,
        config: MobShieldConfig,
        listener: MobShieldListener,
        periodicIntervalMsOverride: Long? = null,
        terminate: () -> Unit = {},
    ): MobShieldEngine =
        MobShieldEngine(
            config = config,
            listener = listener,
            resolveModules = { ModuleRegistry.getAll() },
            scope = scope,
            signalSetVersion = MobShield.SIGNAL_SET_VERSION,
            periodicIntervalMsOverride = periodicIntervalMsOverride,
            terminate = terminate,
        )

    private fun rootModule(
        weight: Int = 90,
        confidence: Int = 100,
    ): DetectionModule =
        object : DetectionModule {
            override val name: String = "mock-root"
            override val criticality: Int = 10

            override suspend fun scan(): List<Signal> =
                listOf(Signal(name = "android.root.mock", weight = weight, confidence = confidence))
        }

    private fun event(severity: Severity): ThreatEvent =
        ThreatEvent.PrivilegedAccess(
            severity = severity,
            signals = listOf("mock"),
            score = 50,
            timestampMs = 0L,
        )

    private class RecordingListener : MobShieldListener {
        val threats = mutableListOf<ThreatEvent>()
        val finished = mutableListOf<List<ThreatEvent>>()

        override fun onThreat(event: ThreatEvent) {
            threats.add(event)
        }

        override fun onAllChecksFinished(events: List<ThreatEvent>) {
            finished.add(events)
        }
    }
}

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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

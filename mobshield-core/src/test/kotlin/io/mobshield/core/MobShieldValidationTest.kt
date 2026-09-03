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

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobShieldValidationTest {
    @After
    fun tearDown() {
        MobShield.resetForTests()
    }

    @Test
    fun run_capturesPerModuleSignals_inCriticalityOrder() =
        runTest {
            val report =
                MobShieldValidation.run(
                    modules =
                        listOf(
                            stub("debugger", 80, signal("android.debug.timing", 55, 65)),
                            stub("root", 90, signal("android.root.mount", 85, 90)),
                        ),
                )

            assertEquals(listOf("root", "debugger"), report.modules.map { it.moduleName })
            assertEquals(90, report.modules.first().criticality)
            assertTrue(report.modules.all { it.didFire })
            assertEquals(listOf("android.root.mount", "android.debug.timing"), report.rawSignals.map { it.name })
        }

    @Test
    fun run_aggregatesSignalsIntoThreatEvents() =
        runTest {
            val report =
                MobShieldValidation.run(
                    modules =
                        listOf(
                            stub("root", 90, signal("android.root.mount", 85, 90)),
                            stub("hooks", 85, signal("common.hook.frida_maps", 80, 90)),
                        ),
                )

            assertEquals(
                setOf(ThreatType.PRIVILEGED_ACCESS, ThreatType.HOOK_FRAMEWORK),
                report.activeThreatTypes,
            )
            assertEquals(Severity.CRITICAL, report.highestSeverity)
            assertTrue(report.firedSignalNames.contains("android.root.mount"))
        }

    @Test
    fun run_moduleThatEmitsNothing_doesNotFire() =
        runTest {
            val report = MobShieldValidation.run(modules = listOf(stub("root", 90)))

            assertEquals(1, report.modules.size)
            assertFalse(report.modules[0].didFire)
            assertTrue(report.events.isEmpty())
            assertNull(report.highestSeverity)
            assertTrue(report.firedSignalNames.isEmpty())
        }

    @Test
    fun run_noModules_producesEmptyReport() =
        runTest {
            val report = MobShieldValidation.run(modules = emptyList())

            assertTrue(report.modules.isEmpty())
            assertTrue(report.events.isEmpty())
            assertTrue(report.rawSignals.isEmpty())
            assertNull(report.highestSeverity)
        }

    @Test
    fun runRegistered_usesModulesFromRegistry() =
        runTest {
            ModuleRegistry.register(stub("root", 90, signal("android.root.mount", 85, 90)))

            val report = MobShieldValidation.runRegistered()

            assertEquals(listOf("root"), report.modules.map { it.moduleName })
            assertTrue(report.activeThreatTypes.contains(ThreatType.PRIVILEGED_ACCESS))
        }

    @Test
    fun run_preservesEvidenceForInspection() =
        runTest {
            val report =
                MobShieldValidation.run(
                    modules =
                        listOf(
                            stub(
                                "root",
                                90,
                                Signal(
                                    name = "android.root.mount",
                                    weight = 85,
                                    confidence = 90,
                                    evidence = mapOf("path" to "/system/xbin/su"),
                                ),
                            ),
                        ),
                )

            assertEquals("/system/xbin/su", report.rawSignals.first().evidence["path"])
        }

    private fun signal(
        name: String,
        weight: Int,
        confidence: Int,
    ): Signal = Signal(name = name, weight = weight, confidence = confidence)

    private fun stub(
        name: String,
        criticality: Int,
        vararg signals: Signal,
    ): DetectionModule =
        object : DetectionModule {
            override val name: String = name
            override val criticality: Int = criticality

            override suspend fun scan(): List<Signal> = signals.toList()
        }
}

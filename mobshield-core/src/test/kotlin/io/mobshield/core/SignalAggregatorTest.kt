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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalAggregatorTest {
    private val defaultConfig = MobShieldConfig()

    @Test
    fun emptySignals_returnsEmpty() {
        val result = SignalAggregator(defaultConfig).aggregate(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun unknownSignalName_isIgnored() {
        val result =
            SignalAggregator(defaultConfig).aggregate(
                listOf(signal("unknown.signal", 100, 100)),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun singleRootSignal_emitsPrivilegedAccess() {
        val result =
            SignalAggregator(defaultConfig).aggregate(
                listOf(signal("android.root.mount_namespace", 80, 100)),
            )
        assertEquals(1, result.size)
        assertEquals(ThreatType.PRIVILEGED_ACCESS, result[0].type)
        assertEquals(80, result[0].score)
    }

    @Test
    fun singleHookSignal_emitsHookFramework() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.hook.frida_port", 50, 80)))
                .single()
        assertEquals(ThreatType.HOOK_FRAMEWORK, event.type)
        assertEquals(40, event.score)
    }

    @Test
    fun multiSignalSameType_combinesScore() {
        val events =
            SignalAggregator(defaultConfig).aggregate(
                listOf(
                    signal("android.root.mount_namespace", 40, 100),
                    signal("android.root.maps_artifact", 40, 100),
                ),
            )
        assertEquals(80, events.single().score)
    }

    @Test
    fun scoreCapsAt100() {
        val events =
            SignalAggregator(defaultConfig).aggregate(
                listOf(
                    signal("android.root.a", 60, 100),
                    signal("android.root.b", 60, 100),
                ),
            )
        assertEquals(100, events.single().score)
    }

    @Test
    fun zeroWeight_producesNoEvent() {
        val result =
            SignalAggregator(defaultConfig).aggregate(
                listOf(signal("android.root.test", 0, 100)),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun zeroConfidence_producesNoEvent() {
        val result =
            SignalAggregator(defaultConfig).aggregate(
                listOf(signal("android.root.test", 50, 0)),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun privilegedAccess_criticalSeverity_atHighScore() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.root.test", 90, 100)))
                .single()
        assertEquals(Severity.CRITICAL, event.severity)
    }

    @Test
    fun privilegedAccess_warningSeverity_atMediumScore() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.root.test", 45, 100)))
                .single()
        assertEquals(Severity.HIGH, event.severity)
    }

    @Test
    fun debugger_mapsCorrectly() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.debug.tracerpid", 60, 100)))
                .single()
        assertEquals(ThreatType.DEBUGGER, event.type)
    }

    @Test
    fun emulator_mapsCorrectly() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.env.qemu_props", 30, 100)))
                .single()
        assertEquals(ThreatType.EMULATOR, event.type)
    }

    @Test
    fun automation_mapsCorrectly() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.automation.appium", 35, 100)))
                .single()
        assertEquals(ThreatType.AUTOMATION, event.type)
    }

    @Test
    fun integrity_mapsCorrectly() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.integrity.signature", 55, 100)))
                .single()
        assertEquals(ThreatType.APP_INTEGRITY, event.type)
    }

    @Test
    fun unofficialStore_mapsCorrectly() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.store.installer", 25, 100)))
                .single()
        assertEquals(ThreatType.UNOFFICIAL_STORE, event.type)
    }

    @Test
    fun developerMode_neverCritical() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.dev.options", 100, 100)))
                .single()
        assertEquals(ThreatType.DEVELOPER_MODE, event.type)
        assertTrue(event.severity != Severity.CRITICAL)
    }

    @Test
    fun adbEnabled_neverCritical() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("android.adb.enabled", 100, 100)))
                .single()
        assertEquals(ThreatType.ADB_ENABLED, event.type)
        assertTrue(event.severity != Severity.CRITICAL)
    }

    @Test
    fun suppressDeveloperSignals_whenDisabled() {
        val config = MobShieldConfig(allowDeveloperSignals = false)
        val result =
            SignalAggregator(config).aggregate(
                listOf(
                    signal("android.dev.options", 100, 100),
                    signal("android.adb.enabled", 100, 100),
                ),
            )
        assertTrue(result.isEmpty())
    }

    @Test
    fun multipleThreatTypes_returnsMultipleEvents() {
        val events =
            SignalAggregator(defaultConfig).aggregate(
                listOf(
                    signal("android.root.test", 80, 100),
                    signal("android.hook.frida_port", 70, 100),
                ),
            )
        assertEquals(2, events.size)
        assertEquals(
            setOf(ThreatType.PRIVILEGED_ACCESS, ThreatType.HOOK_FRAMEWORK),
            events.map { it.type }.toSet(),
        )
    }

    @Test
    fun events_sortedByScoreDescending() {
        val events =
            SignalAggregator(defaultConfig).aggregate(
                listOf(
                    signal("android.env.qemu_props", 30, 100),
                    signal("android.root.test", 90, 100),
                ),
            )
        assertTrue(events.first().score >= events.last().score)
    }

    @Test
    fun evidence_mergedIntoMetadata() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(
                    listOf(
                        Signal(
                            name = "android.root.test",
                            weight = 80,
                            confidence = 100,
                            evidence = mapOf("detail" to "mount"),
                        ),
                    ),
                ).single()
        assertEquals("mount", event.metadata["android.root.test.detail"])
    }

    @Test
    fun customThresholds_respected() {
        val config =
            MobShieldConfig(
                thresholds =
                    DefaultThreatThresholds.map +
                        (ThreatType.EMULATOR to ThreatThreshold(10, 15)),
            )
        val event =
            SignalAggregator(config)
                .aggregate(listOf(signal("android.env.qemu_props", 16, 100)))
                .single()
        assertEquals(Severity.CRITICAL, event.severity)
    }

    @Test
    fun commonHookPrefix_mapsToHookFramework() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("common.hook.prologue", 65, 100)))
                .single()
        assertEquals(ThreatType.HOOK_FRAMEWORK, event.type)
    }

    @Test
    fun iosJailbreak_mapsToPrivilegedAccess() {
        val event =
            SignalAggregator(defaultConfig)
                .aggregate(listOf(signal("ios.jb.dyld_image", 70, 100)))
                .single()
        assertEquals(ThreatType.PRIVILEGED_ACCESS, event.type)
    }

    @Test
    fun lowScoreBelowWarning_emitsInfoOrSkipped() {
        val result =
            SignalAggregator(defaultConfig).aggregate(
                listOf(signal("android.root.test", 5, 50)),
            )
        assertTrue(result.isEmpty() || result.single().severity == Severity.INFO)
    }

    @Test
    fun resolveThreatType_coversKnownPrefixes() {
        assertEquals(
            ThreatType.DEBUGGER,
            SignalAggregator.resolveThreatType("android.debug.ptrace"),
        )
        assertEquals(
            ThreatType.HOOK_FRAMEWORK,
            SignalAggregator.resolveThreatType("ios.hook.dyld_insert"),
        )
    }

    private fun signal(
        name: String,
        weight: Int,
        confidence: Int,
    ): Signal = Signal(name = name, weight = weight, confidence = confidence, timestampMs = 1L)
}

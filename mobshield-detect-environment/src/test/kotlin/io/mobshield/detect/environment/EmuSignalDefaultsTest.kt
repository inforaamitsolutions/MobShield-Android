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
package io.mobshield.detect.environment

import io.mobshield.core.DefaultThreatThresholds
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.Severity
import io.mobshield.core.SignalAggregator
import io.mobshield.core.ThreatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EmuSignalDefaultsTest {
    @Test
    fun stackedEmulatorSignals_stayBelowCriticalOnDefaults() {
        val signals =
            listOf(
                EmuSignalDefaults.QEMU_PROPS,
                EmuSignalDefaults.QEMU_DEVICE,
                EmuSignalDefaults.BUILD_FINGERPRINT,
            ).map { name ->
                EmuSignalDefaults.buildSignal(name, mapOf("detail" to "test"), emptyMap())
            }
        val event = SignalAggregator(MobShieldConfig()).aggregate(signals).single()
        assertEquals(ThreatType.EMULATOR, event.type)
        val critical = DefaultThreatThresholds.map.getValue(ThreatType.EMULATOR).critical
        assertNotEquals(Severity.CRITICAL, event.severity)
        require(critical != null)
        assert(event.score < critical)
    }
}

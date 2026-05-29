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
package io.mobshield.detect.debugger

import io.mobshield.core.SignalAggregator
import io.mobshield.core.ThreatType
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugSignalDefaultsTest {
    @Test
    fun adbSignal_mapsToAdbEnabledThreat() {
        val signal =
            DebugSignalDefaults.buildSignal(
                DebugSignalDefaults.ADB_ENABLED,
                mapOf("adb" to "true"),
                emptyMap(),
            )
        val event = SignalAggregator(io.mobshield.core.MobShieldConfig()).aggregate(listOf(signal)).single()
        assertEquals(ThreatType.ADB_ENABLED, event.type)
    }
}

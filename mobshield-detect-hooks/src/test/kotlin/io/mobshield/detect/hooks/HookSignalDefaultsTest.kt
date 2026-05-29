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
package io.mobshield.detect.hooks

import io.mobshield.core.SignalTuning
import org.junit.Assert.assertEquals
import org.junit.Test

class HookSignalDefaultsTest {
    @Test
    fun buildSignal_appliesTuningOverride() {
        val signal =
            HookSignalDefaults.buildSignal(
                HookSignalDefaults.FRIDA_PORT,
                mapOf("port" to "27042"),
                mapOf(HookSignalDefaults.FRIDA_PORT to SignalTuning(weight = 99, confidence = 91)),
            )
        assertEquals(99, signal.weight)
        assertEquals(91, signal.confidence)
    }
}

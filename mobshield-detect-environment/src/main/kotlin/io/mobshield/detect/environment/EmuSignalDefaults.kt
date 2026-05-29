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

import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning

object EmuSignalDefaults {
    const val QEMU_PROPS = "android.env.qemu_props"
    const val QEMU_DEVICE = "android.env.qemu_device"
    const val CPU_GOLDFISH = "android.env.cpu_goldfish"
    const val BUILD_FINGERPRINT = "android.env.build_fingerprint"
    const val SENSOR_COUNT = "android.env.sensor_count"
    const val AUTOMATION = "android.automation.framework"

    private val defaults: Map<String, SignalTuning> =
        mapOf(
            QEMU_PROPS to SignalTuning(weight = 22, confidence = 65),
            QEMU_DEVICE to SignalTuning(weight = 28, confidence = 75),
            CPU_GOLDFISH to SignalTuning(weight = 24, confidence = 70),
            BUILD_FINGERPRINT to SignalTuning(weight = 20, confidence = 60),
            SENSOR_COUNT to SignalTuning(weight = 18, confidence = 55),
            AUTOMATION to SignalTuning(weight = 30, confidence = 70),
        )

    fun buildSignal(
        name: String,
        evidence: Map<String, String>,
        tuning: Map<String, SignalTuning>,
        overrideTuning: SignalTuning? = null,
    ): Signal {
        val resolved = overrideTuning ?: tuning[name] ?: defaults[name] ?: SignalTuning(15, 50)
        return Signal(name = name, weight = resolved.weight, confidence = resolved.confidence, evidence = evidence)
    }
}

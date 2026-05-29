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
package io.mobshield.detect.integrity

import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning

object IntegritySignalDefaults {
    const val SIGNATURE = "android.integrity.signature"
    const val INSTALLER = "android.store.installer"
    const val APK_CHECKSUM = "android.integrity.apk_checksum"
    const val NATIVE_LIB_CHECKSUM = "android.integrity.native_lib_checksum"

    private val defaults: Map<String, SignalTuning> =
        mapOf(
            SIGNATURE to SignalTuning(weight = 90, confidence = 95),
            INSTALLER to SignalTuning(weight = 25, confidence = 45),
            APK_CHECKSUM to SignalTuning(weight = 85, confidence = 90),
            NATIVE_LIB_CHECKSUM to SignalTuning(weight = 80, confidence = 85),
        )

    fun buildSignal(
        name: String,
        evidence: Map<String, String>,
        tuning: Map<String, SignalTuning>,
        overrideTuning: SignalTuning? = null,
    ): Signal {
        val resolved = overrideTuning ?: tuning[name] ?: defaults[name] ?: SignalTuning(50, 50)
        return Signal(name = name, weight = resolved.weight, confidence = resolved.confidence, evidence = evidence)
    }
}

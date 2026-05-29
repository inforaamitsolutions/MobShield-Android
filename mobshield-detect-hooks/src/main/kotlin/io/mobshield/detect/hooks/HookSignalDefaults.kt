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

import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning

object HookSignalDefaults {
    const val FRIDA_MAPS = "common.hook.frida_maps"
    const val PROLOGUE = "common.hook.prologue"
    const val FRIDA_PORT = "android.hook.frida_port"
    const val THREAD_NAME = "android.hook.thread_name"
    const val ART_DEX = "android.hook.art_dex"
    const val SUSPICIOUS_LIBRARY = "android.hook.suspicious_library"
    const val XPOSED_CLASS = "android.hook.xposed"
    const val STACK_LEAK = "android.hook.stack_leak"

    private val defaults: Map<String, SignalTuning> =
        mapOf(
            FRIDA_MAPS to SignalTuning(weight = 75, confidence = 85),
            PROLOGUE to SignalTuning(weight = 80, confidence = 90),
            FRIDA_PORT to SignalTuning(weight = 85, confidence = 95),
            THREAD_NAME to SignalTuning(weight = 70, confidence = 80),
            ART_DEX to SignalTuning(weight = 75, confidence = 85),
            SUSPICIOUS_LIBRARY to SignalTuning(weight = 55, confidence = 65),
            XPOSED_CLASS to SignalTuning(weight = 70, confidence = 80),
            STACK_LEAK to SignalTuning(weight = 65, confidence = 75),
        )

    fun buildSignal(
        name: String,
        evidence: Map<String, String>,
        tuning: Map<String, SignalTuning>,
    ): Signal {
        val resolved = tuning[name] ?: defaults[name] ?: SignalTuning(weight = 40, confidence = 50)
        return Signal(
            name = name,
            weight = resolved.weight,
            confidence = resolved.confidence,
            evidence = evidence,
        )
    }
}

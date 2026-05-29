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

import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning

object DebugSignalDefaults {
    const val TRACERPID = "android.debug.tracerpid"
    const val PTRACE = "android.debug.ptrace"
    const val TIMING = "android.debug.timing"
    const val APP_DEBUGGABLE = "android.debug.app_debuggable"
    const val ADB_ENABLED = "android.adb.enabled"
    const val WAITING_DEBUGGER = "android.debug.waiting"

    private val defaults: Map<String, SignalTuning> =
        mapOf(
            TRACERPID to SignalTuning(weight = 85, confidence = 90),
            PTRACE to SignalTuning(weight = 80, confidence = 85),
            TIMING to SignalTuning(weight = 65, confidence = 70),
            APP_DEBUGGABLE to SignalTuning(weight = 70, confidence = 60),
            ADB_ENABLED to SignalTuning(weight = 30, confidence = 40),
            WAITING_DEBUGGER to SignalTuning(weight = 90, confidence = 95),
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

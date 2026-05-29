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
package io.mobshield.detect.debugger.internal

internal object DebugNativeBridge {
    const val RESULT_OK = 0
    const val RESULT_DETECTED = 1
    const val RESULT_UNAVAILABLE = 2

    init {
        System.loadLibrary("mobshielddebug")
    }

    fun tracerPidCheck(): NativeCheckResult = runCheck(::nativeTracerPidCheck, ::nativeTracerPidEvidence)

    fun ptraceSelf(): NativeCheckResult = runCheck(::nativePtraceSelf, ::nativePtraceEvidence)

    fun timingCheck(): NativeCheckResult = runCheck(::nativeTimingCheck, ::nativeTimingEvidence)

    private fun runCheck(
        codeFn: () -> Int,
        evidenceFn: () -> String,
    ): NativeCheckResult {
        val code = codeFn()
        val evidence = if (code == RESULT_DETECTED) evidenceFn() else ""
        return NativeCheckResult(code, evidence)
    }

    @JvmStatic private external fun nativeTracerPidCheck(): Int

    @JvmStatic private external fun nativeTracerPidEvidence(): String

    @JvmStatic private external fun nativePtraceSelf(): Int

    @JvmStatic private external fun nativePtraceEvidence(): String

    @JvmStatic private external fun nativeTimingCheck(): Int

    @JvmStatic private external fun nativeTimingEvidence(): String
}

internal data class NativeCheckResult(val code: Int, val evidence: String)

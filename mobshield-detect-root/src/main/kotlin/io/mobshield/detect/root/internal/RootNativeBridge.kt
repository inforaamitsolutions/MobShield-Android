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
package io.mobshield.detect.root.internal

internal object RootNativeBridge {
    const val RESULT_OK = 0
    const val RESULT_DETECTED = 1
    const val RESULT_ERROR = -1

    init {
        System.loadLibrary("mobshieldroot")
    }

    fun mountNamespaceCheck(): NativeCheckResult = runCheck(::nativeMountNamespaceCheck, ::nativeMountNamespaceEvidence)

    fun magiskUdsProbe(): NativeCheckResult = runCheck(::nativeMagiskUdsProbe, ::nativeMagiskUdsEvidence)

    fun overlayfsCheck(): NativeCheckResult = runCheck(::nativeOverlayfsCheck, ::nativeOverlayfsEvidence)

    fun errnoDeviation(): NativeCheckResult = runCheck(::nativeErrnoDeviation, ::nativeErrnoEvidence)

    fun zygiskLoaderScan(): NativeCheckResult = runCheck(::nativeZygiskLoaderScan, ::nativeZygiskEvidence)

    fun kernelsuCheck(): NativeCheckResult = runCheck(::nativeKernelsuCheck, ::nativeKernelsuEvidence)

    fun readSystemProperty(key: String): String = nativeReadSystemProperty(key)

    private fun runCheck(
        codeFn: () -> Int,
        evidenceFn: () -> String,
    ): NativeCheckResult {
        val code = codeFn()
        val evidence = if (code == RESULT_DETECTED) evidenceFn() else ""
        return NativeCheckResult(code = code, evidence = evidence)
    }

    @JvmStatic
    private external fun nativeMountNamespaceCheck(): Int

    @JvmStatic
    private external fun nativeMountNamespaceEvidence(): String

    @JvmStatic
    private external fun nativeMagiskUdsProbe(): Int

    @JvmStatic
    private external fun nativeMagiskUdsEvidence(): String

    @JvmStatic
    private external fun nativeOverlayfsCheck(): Int

    @JvmStatic
    private external fun nativeOverlayfsEvidence(): String

    @JvmStatic
    private external fun nativeErrnoDeviation(): Int

    @JvmStatic
    private external fun nativeErrnoEvidence(): String

    @JvmStatic
    private external fun nativeZygiskLoaderScan(): Int

    @JvmStatic
    private external fun nativeZygiskEvidence(): String

    @JvmStatic
    private external fun nativeKernelsuCheck(): Int

    @JvmStatic
    private external fun nativeKernelsuEvidence(): String

    @JvmStatic
    private external fun nativeReadSystemProperty(key: String): String
}

internal data class NativeCheckResult(
    val code: Int,
    val evidence: String,
)

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

import android.content.Context
import io.mobshield.core.DetectionModule
import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning
import io.mobshield.detect.debugger.internal.DebugNativeBridge
import io.mobshield.detect.debugger.internal.NativeCheckResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

class DebugDetectionModule(
    context: Context,
    private val detectionTuning: Map<String, SignalTuning> = emptyMap(),
    private val nativeTimeoutMs: Long = DEFAULT_NATIVE_TIMEOUT_MS,
) : DetectionModule {
    private val appContext = context.applicationContext
    private val appDebuggable = AppDebuggable(appContext)
    private val adbCheck = AdbEnabledCheck(appContext)

    override val name: String = MODULE_NAME

    override val criticality: Int = CRITICALITY

    override suspend fun scan(): List<Signal> =
        coroutineScope {
            listOf(
                async { runNative(DebugSignalDefaults.TRACERPID) { DebugNativeBridge.tracerPidCheck() } },
                async { runNative(DebugSignalDefaults.PTRACE) { DebugNativeBridge.ptraceSelf() } },
                async { runNative(DebugSignalDefaults.TIMING) { DebugNativeBridge.timingCheck() } },
                async { runKotlin { scanAppDebuggable() } },
                async { runKotlin { scanAdb() } },
                async { runKotlin { scanWaitingDebugger() } },
            ).flatMap { it.await() }
        }

    private suspend fun runNative(
        signal: String,
        block: () -> NativeCheckResult,
    ): List<Signal> =
        try {
            val result = withTimeout(nativeTimeoutMs) { block() }
            toSignal(signal, result, informational = false)
        } catch (_: Exception) {
            emptyList()
        }

    private suspend fun runKotlin(block: suspend () -> List<Signal>): List<Signal> =
        try {
            withTimeout(nativeTimeoutMs) { block() }
        } catch (_: Exception) {
            emptyList()
        }

    private fun scanAppDebuggable(): List<Signal> {
        val hit = appDebuggable.scan() ?: return emptyList()
        val weight = if (hit.isReleaseBuild) 70 else 0
        if (weight == 0) {
            return emptyList()
        }
        return listOf(
            DebugSignalDefaults.buildSignal(
                DebugSignalDefaults.APP_DEBUGGABLE,
                mapOf("releaseBuild" to hit.isReleaseBuild.toString()),
                detectionTuning,
                SignalTuning(weight = weight, confidence = 60),
            ),
        )
    }

    private fun scanAdb(): List<Signal> {
        val hit = adbCheck.scan() ?: return emptyList()
        return listOf(
            DebugSignalDefaults.buildSignal(
                DebugSignalDefaults.ADB_ENABLED,
                mapOf(
                    "adb" to hit.adbEnabled.toString(),
                    "usb" to hit.usbDebugging.toString(),
                ),
                detectionTuning,
                SignalTuning(weight = 30, confidence = 40),
            ),
        )
    }

    private fun scanWaitingDebugger(): List<Signal> {
        val hit = WaitingForDebugger.scan() ?: return emptyList()
        return listOf(
            DebugSignalDefaults.buildSignal(
                DebugSignalDefaults.WAITING_DEBUGGER,
                mapOf("waiting" to hit.waiting.toString(), "connected" to hit.connected.toString()),
                detectionTuning,
            ),
        )
    }

    private fun toSignal(
        name: String,
        result: NativeCheckResult,
        informational: Boolean,
    ): List<Signal> {
        if (result.code != DebugNativeBridge.RESULT_DETECTED) {
            return emptyList()
        }
        return listOf(
            DebugSignalDefaults.buildSignal(
                name,
                mapOf("detail" to result.evidence),
                detectionTuning,
            ),
        )
    }

    companion object {
        const val MODULE_NAME = "debug"
        const val CRITICALITY = 100
        val DEFAULT_NATIVE_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(50)
    }
}

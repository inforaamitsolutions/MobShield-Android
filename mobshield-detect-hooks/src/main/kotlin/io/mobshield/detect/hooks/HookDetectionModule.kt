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

import android.content.Context
import io.mobshield.core.DetectionModule
import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning
import io.mobshield.detect.hooks.internal.HooksNativeBridge
import io.mobshield.detect.hooks.internal.NativeCheckResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

/** Detects Frida, objection, LSPosed, and Xposed hook frameworks. */
class HookDetectionModule(
    context: Context,
    private val detectionTuning: Map<String, SignalTuning> = emptyMap(),
    private val nativeTimeoutMs: Long = DEFAULT_NATIVE_TIMEOUT_MS,
) : DetectionModule {
    private val appContext = context.applicationContext
    private val classLoader = appContext.classLoader
    private val suspiciousLibraryScan = SuspiciousLibraryScan()
    private val xposedQuery = XposedModuleQuery(classLoader)

    override val name: String = MODULE_NAME

    override val criticality: Int = CRITICALITY

    override suspend fun scan(): List<Signal> =
        coroutineScope {
            val checks =
                listOf(
                    async { runNativeCheck(HookSignalDefaults.FRIDA_MAPS) { HooksNativeBridge.procMapsScan() } },
                    async {
                        runNativeCheck(
                            HookSignalDefaults.PROLOGUE,
                        ) { HooksNativeBridge.functionPrologueInspect() }
                    },
                    async { runNativeCheck(HookSignalDefaults.FRIDA_PORT) { HooksNativeBridge.fridaPortProbe() } },
                    async { runNativeCheck(HookSignalDefaults.THREAD_NAME) { HooksNativeBridge.threadNameScan() } },
                    async {
                        runNativeCheck(
                            HookSignalDefaults.ART_DEX,
                        ) { HooksNativeBridge.artInspection(classLoader) }
                    },
                    async { runKotlinCheck { scanSuspiciousLibraries() } },
                    async { runKotlinCheck { scanXposedClasses() } },
                    async { runKotlinCheck { scanStackLeaks() } },
                )
            checks.flatMap { it.await() }
        }

    private suspend fun runNativeCheck(
        signalName: String,
        block: () -> NativeCheckResult,
    ): List<Signal> =
        try {
            val result = withTimeout(nativeTimeoutMs) { block() }
            toSignal(signalName, result)
        } catch (_: Exception) {
            emptyList()
        }

    private suspend fun runKotlinCheck(block: suspend () -> List<Signal>): List<Signal> =
        try {
            withTimeout(nativeTimeoutMs) { block() }
        } catch (_: Exception) {
            emptyList()
        }

    private fun scanSuspiciousLibraries(): List<Signal> {
        val hit = suspiciousLibraryScan.scan() ?: return emptyList()
        return listOf(
            HookSignalDefaults.buildSignal(
                HookSignalDefaults.SUSPICIOUS_LIBRARY,
                mapOf("library" to hit.library),
                detectionTuning,
            ),
        )
    }

    private fun scanXposedClasses(): List<Signal> {
        val hit = xposedQuery.scan() ?: return emptyList()
        return listOf(
            HookSignalDefaults.buildSignal(
                HookSignalDefaults.XPOSED_CLASS,
                mapOf("class" to hit.className),
                detectionTuning,
            ),
        )
    }

    private fun scanStackLeaks(): List<Signal> {
        val hit = ClassNameLeakScan.scan() ?: return emptyList()
        return listOf(
            HookSignalDefaults.buildSignal(
                HookSignalDefaults.STACK_LEAK,
                mapOf("class" to hit.className, "method" to hit.methodName),
                detectionTuning,
            ),
        )
    }

    private fun toSignal(
        signalName: String,
        result: NativeCheckResult,
    ): List<Signal> {
        if (result.code != HooksNativeBridge.RESULT_DETECTED) {
            return emptyList()
        }
        return listOf(
            HookSignalDefaults.buildSignal(
                signalName,
                mapOf("detail" to result.evidence),
                detectionTuning,
            ),
        )
    }

    companion object {
        const val MODULE_NAME = "hooks"
        const val CRITICALITY = 100
        val DEFAULT_NATIVE_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(100)
    }
}

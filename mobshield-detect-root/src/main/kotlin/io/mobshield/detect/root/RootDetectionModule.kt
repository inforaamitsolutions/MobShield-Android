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
package io.mobshield.detect.root

import android.content.Context
import io.mobshield.core.DetectionModule
import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning
import io.mobshield.detect.root.internal.NativeCheckResult
import io.mobshield.detect.root.internal.RootNativeBridge
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

/**
 * Android root detection module producing [Signal] values for Magisk, Zygisk, Shamiko, and KernelSU.
 */
class RootDetectionModule(
    context: Context,
    private val detectionTuning: Map<String, SignalTuning> = emptyMap(),
    private val nativeTimeoutMs: Long = DEFAULT_NATIVE_TIMEOUT_MS,
) : DetectionModule {
    private val appContext = context.applicationContext
    private val packagesScan = DangerousPackagesScan(appContext.packageManager)

    override val name: String = MODULE_NAME

    override val criticality: Int = 90

    override suspend fun scan(): List<Signal> =
        coroutineScope {
            val deferred =
                listOf(
                    async {
                        runNativeCheck(
                            RootSignalDefaults.MOUNT_NAMESPACE,
                        ) { RootNativeBridge.mountNamespaceCheck() }
                    },
                    async { runNativeCheck(RootSignalDefaults.MAGISK_UDS) { RootNativeBridge.magiskUdsProbe() } },
                    async { runNativeCheck(RootSignalDefaults.OVERLAYFS) { RootNativeBridge.overlayfsCheck() } },
                    async { runNativeCheck(RootSignalDefaults.ERRNO_DEVIATION) { RootNativeBridge.errnoDeviation() } },
                    async { runNativeCheck(RootSignalDefaults.ZYGISK_MAPS) { RootNativeBridge.zygiskLoaderScan() } },
                    async { runNativeCheck(RootSignalDefaults.KERNELSU) { RootNativeBridge.kernelsuCheck() } },
                    async { runKotlinCheck { scanSuPaths() } },
                    async { runKotlinCheck { scanDangerousPackages() } },
                    async { runKotlinCheck { scanSystemProperties() } },
                )
            deferred.flatMap { it.await() }
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
            withTimeout(nativeTimeoutMs) {
                block()
            }
        } catch (_: Exception) {
            emptyList()
        }

    private fun scanSuPaths(): List<Signal> {
        val hit = SuPathScan.scan() ?: return emptyList()
        return listOf(
            RootSignalDefaults.buildSignal(
                RootSignalDefaults.PATH_PROBE,
                mapOf("path" to hit.path),
                detectionTuning,
            ),
        )
    }

    private fun scanDangerousPackages(): List<Signal> {
        val hit = packagesScan.scan() ?: return emptyList()
        return listOf(
            RootSignalDefaults.buildSignal(
                RootSignalDefaults.DANGEROUS_PACKAGES,
                mapOf("package" to hit.packageName, "label" to hit.label),
                detectionTuning,
            ),
        )
    }

    private fun scanSystemProperties(): List<Signal> {
        val hit = SystemPropertiesProbe.scan() ?: return emptyList()
        return listOf(
            RootSignalDefaults.buildSignal(
                RootSignalDefaults.PROPS,
                hit.properties.mapValues { it.value },
                detectionTuning,
            ),
        )
    }

    private fun toSignal(
        signalName: String,
        result: NativeCheckResult,
    ): List<Signal> {
        if (result.code != RootNativeBridge.RESULT_DETECTED) {
            return emptyList()
        }
        return listOf(
            RootSignalDefaults.buildSignal(
                signalName,
                mapOf("detail" to result.evidence),
                detectionTuning,
            ),
        )
    }

    companion object {
        const val MODULE_NAME = "mobshield-detect-root"
        val DEFAULT_NATIVE_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(50)
    }
}

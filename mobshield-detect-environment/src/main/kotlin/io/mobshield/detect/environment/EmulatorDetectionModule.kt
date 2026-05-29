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

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import io.mobshield.core.DetectionModule
import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning
import io.mobshield.detect.environment.internal.EmuNativeBridge
import io.mobshield.detect.environment.internal.NativeCheckResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

class EmulatorDetectionModule(
    context: Context,
    private val detectionTuning: Map<String, SignalTuning> = emptyMap(),
    private val nativeTimeoutMs: Long = DEFAULT_NATIVE_TIMEOUT_MS,
    buildFingerprintCheck: BuildFingerprintCheck = BuildFingerprintCheck(),
    sensorCountCheck: SensorCountCheck? = null,
    automationFrameworkCheck: AutomationFrameworkCheck? = null,
) : DetectionModule {
    private val appContext = context.applicationContext
    private val buildCheck = buildFingerprintCheck
    private val sensorCheck =
        sensorCountCheck
            ?: SensorCountCheck(sensorCountProvider = { countSensors(appContext) })
    private val automationCheck =
        automationFrameworkCheck
            ?: AutomationFrameworkCheck(
                runningServiceNames = { listRunningServiceClassNames(appContext) },
                accessibilityServiceIds = { listEnabledAccessibilityServiceIds(appContext) },
            )

    override val name: String = MODULE_NAME

    override val criticality: Int = CRITICALITY

    override suspend fun scan(): List<Signal> =
        coroutineScope {
            listOf(
                async { runNative(EmuSignalDefaults.QEMU_PROPS) { EmuNativeBridge.qemuProps() } },
                async { runNative(EmuSignalDefaults.QEMU_DEVICE) { EmuNativeBridge.qemuDevices() } },
                async { runNative(EmuSignalDefaults.CPU_GOLDFISH) { EmuNativeBridge.cpuFeatures() } },
                async { runKotlin { scanBuildFingerprint() } },
                async { runKotlin { scanSensorCount() } },
                async { runKotlin { scanAutomation() } },
            ).flatMap { it.await() }
        }

    private suspend fun runNative(
        signal: String,
        block: () -> NativeCheckResult,
    ): List<Signal> =
        try {
            val result = withTimeout(nativeTimeoutMs) { block() }
            toSignal(signal, result)
        } catch (_: Exception) {
            emptyList()
        }

    private suspend fun runKotlin(block: suspend () -> List<Signal>): List<Signal> =
        try {
            withTimeout(nativeTimeoutMs) { block() }
        } catch (_: Exception) {
            emptyList()
        }

    private fun scanBuildFingerprint(): List<Signal> {
        val hit = buildCheck.scan() ?: return emptyList()
        return listOf(
            EmuSignalDefaults.buildSignal(
                EmuSignalDefaults.BUILD_FINGERPRINT,
                mapOf("matched" to hit.matched.joinToString(","), "fingerprint" to hit.fingerprint),
                detectionTuning,
            ),
        )
    }

    private fun scanSensorCount(): List<Signal> {
        val hit = sensorCheck.scan() ?: return emptyList()
        return listOf(
            EmuSignalDefaults.buildSignal(
                EmuSignalDefaults.SENSOR_COUNT,
                mapOf("count" to hit.count.toString(), "threshold" to hit.threshold.toString()),
                detectionTuning,
            ),
        )
    }

    private fun scanAutomation(): List<Signal> {
        val hit = automationCheck.scan() ?: return emptyList()
        return listOf(
            EmuSignalDefaults.buildSignal(
                EmuSignalDefaults.AUTOMATION,
                mapOf("matches" to hit.matches.joinToString(",")),
                detectionTuning,
            ),
        )
    }

    private fun toSignal(
        name: String,
        result: NativeCheckResult,
    ): List<Signal> {
        if (result.code != EmuNativeBridge.RESULT_DETECTED) {
            return emptyList()
        }
        return listOf(
            EmuSignalDefaults.buildSignal(
                name,
                mapOf("detail" to result.evidence),
                detectionTuning,
            ),
        )
    }

    companion object {
        const val MODULE_NAME = "environment"
        const val CRITICALITY = 35
        val DEFAULT_NATIVE_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(50)

        fun countSensors(context: Context): Int {
            val sensorManager =
                context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
                    ?: return 0
            return sensorManager.getSensorList(Sensor.TYPE_ALL).size
        }

        fun listRunningServiceClassNames(context: Context): List<String> {
            val manager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                    ?: return emptyList()

            @Suppress("DEPRECATION")
            val services = manager.getRunningServices(Int.MAX_VALUE) ?: return emptyList()
            return services.mapNotNull { it.service.className }
        }

        fun listEnabledAccessibilityServiceIds(context: Context): List<String> {
            val enabled =
                android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                )
                    ?: return emptyList()
            return enabled.split(':').filter { it.isNotBlank() }
        }
    }
}

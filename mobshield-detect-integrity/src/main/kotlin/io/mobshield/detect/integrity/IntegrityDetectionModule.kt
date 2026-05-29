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

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.mobshield.core.DetectionModule
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.Signal
import io.mobshield.core.SignalTuning
import io.mobshield.detect.integrity.internal.DigestUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

class IntegrityDetectionModule(
    context: Context,
    private val config: MobShieldConfig,
    private val detectionTuning: Map<String, SignalTuning> = config.detectionTuning,
    private val scanTimeoutMs: Long = DEFAULT_SCAN_TIMEOUT_MS,
) : DetectionModule {
    private val appContext = context.applicationContext
    private val packageName = config.expectedPackageId ?: appContext.packageName
    private val packageManager = appContext.packageManager

    private val signatureCheck =
        SignatureCheck(
            packageName = packageName,
            expectedSigners = config.expectedSigners,
            certificateDigestsProvider = {
                SignatureCheck.certificateDigests(packageManager, packageName)
            },
        )
    private val installerCheck =
        InstallerOriginCheck(
            allowedInstallSources = config.allowedInstallSources,
            installingPackageProvider = { readInstallingPackageName() },
        )
    private val apkChecksumCheck =
        ApkChecksumCheck(
            expectedApkSha256 = config.expectedApkSha256,
            checksumProvider = { ApkChecksumCheck.requestWholeApkSha256(packageManager, packageName) },
        )
    private val nativeLibChecksumCheck =
        NativeLibChecksumCheck(
            expectedNativeLibSha256 = config.expectedNativeLibSha256,
            libraryFileProvider = {
                NativeLibChecksumCheck.coreLibraryFile(appContext.applicationInfo.nativeLibraryDir)
            },
        )

    override val name: String = MODULE_NAME

    override val criticality: Int = CRITICALITY

    override suspend fun scan(): List<Signal> =
        coroutineScope {
            listOf(
                async { runCheck { scanSignature() } },
                async { runCheck { scanInstaller() } },
                async { runCheck { scanApkChecksum() } },
                async { runCheck { scanNativeLibChecksum() } },
            ).flatMap { it.await() }
        }

    private suspend fun runCheck(block: suspend () -> List<Signal>): List<Signal> =
        try {
            withTimeout(scanTimeoutMs) { block() }
        } catch (_: Exception) {
            emptyList()
        }

    private fun scanSignature(): List<Signal> {
        val hit = signatureCheck.scan() ?: return emptyList()
        return listOf(
            IntegritySignalDefaults.buildSignal(
                IntegritySignalDefaults.SIGNATURE,
                mapOf(
                    "reason" to hit.reason,
                    "expectedCount" to hit.expected.size.toString(),
                    "actual" to hit.actual.joinToString(","),
                ),
                detectionTuning,
            ),
        )
    }

    private fun scanInstaller(): List<Signal> {
        val hit = installerCheck.scan() ?: return emptyList()
        return listOf(
            IntegritySignalDefaults.buildSignal(
                IntegritySignalDefaults.INSTALLER,
                mapOf(
                    "installer" to (hit.installer ?: "null"),
                    "allowed" to hit.allowed.joinToString(","),
                ),
                detectionTuning,
                SignalTuning(weight = 25, confidence = 45),
            ),
        )
    }

    private fun scanApkChecksum(): List<Signal> {
        val hit = apkChecksumCheck.scan() ?: return emptyList()
        return listOf(
            IntegritySignalDefaults.buildSignal(
                IntegritySignalDefaults.APK_CHECKSUM,
                mapOf(
                    "reason" to hit.reason,
                    "expected" to DigestUtil.normalizeHex(hit.expected),
                    "actual" to (hit.actual?.let(DigestUtil::normalizeHex) ?: "null"),
                ),
                detectionTuning,
            ),
        )
    }

    private fun scanNativeLibChecksum(): List<Signal> {
        val hit = nativeLibChecksumCheck.scan() ?: return emptyList()
        return listOf(
            IntegritySignalDefaults.buildSignal(
                IntegritySignalDefaults.NATIVE_LIB_CHECKSUM,
                mapOf(
                    "reason" to hit.reason,
                    "path" to (hit.path ?: "null"),
                    "expected" to DigestUtil.normalizeHex(hit.expected),
                    "actual" to (hit.actual?.let(DigestUtil::normalizeHex) ?: "null"),
                ),
                detectionTuning,
            ),
        )
    }

    private fun readInstallingPackageName(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName)
        }
    }

    companion object {
        const val MODULE_NAME = "integrity"
        const val CRITICALITY = 100
        val DEFAULT_SCAN_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(100)
    }
}

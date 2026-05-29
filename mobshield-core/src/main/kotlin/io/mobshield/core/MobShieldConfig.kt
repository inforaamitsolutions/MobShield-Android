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
package io.mobshield.core

/**
 * Runtime configuration for MobShield.
 *
 * @param detectOnly When true, never suggests process termination (spec DETECT_ONLY mode).
 * @param expectedSigners SHA-256 certificate digests (hex, lowercase or uppercase).
 * @param terminationPolicy Optional process exit policy.
 * @param thresholds Per-threat score cutoffs overriding spec defaults.
 * @param allowDeveloperSignals Emit DEVELOPER_MODE and ADB_ENABLED events when true.
 * @param expectedPackageId Optional package name override for integrity checks.
 * @param periodicIntervalSec Optional rescan interval; null disables periodic scans.
 * @param detectionTuning Per-signal weight/confidence overrides keyed by signal name.
 * @param allowedInstallSources Package installer IDs treated as official (Play Store, etc.).
 * @param expectedApkSha256 Optional whole-APK SHA-256 hex for [PackageManager.requestChecksums].
 * @param expectedNativeLibSha256 Optional on-disk SHA-256 hex for `libmobshieldcore.so`.
 */
data class MobShieldConfig(
    val detectOnly: Boolean = true,
    val expectedSigners: List<String> = emptyList(),
    val terminationPolicy: TerminationPolicy = TerminationPolicy.NONE,
    val thresholds: Map<ThreatType, ThreatThreshold> = DefaultThreatThresholds.map,
    val allowDeveloperSignals: Boolean = true,
    val expectedPackageId: String? = null,
    val periodicIntervalSec: Int? = null,
    val detectionTuning: Map<String, SignalTuning> = emptyMap(),
    val allowedInstallSources: Set<String> = DEFAULT_ALLOWED_INSTALL_SOURCES,
    val expectedApkSha256: String? = null,
    val expectedNativeLibSha256: String? = null,
) {
    init {
        validateSigners(expectedSigners)
        expectedApkSha256?.let { validateSha256Hex("expectedApkSha256", it) }
        expectedNativeLibSha256?.let { validateSha256Hex("expectedNativeLibSha256", it) }
        periodicIntervalSec?.let {
            require(it > 0) { "periodicIntervalSec must be positive" }
        }
        expectedPackageId?.let {
            require(it.isNotBlank()) { "expectedPackageId must not be blank" }
        }
        if (detectOnly) {
            require(terminationPolicy == TerminationPolicy.NONE) {
                "detectOnly requires TerminationPolicy.NONE"
            }
        }
    }

    class Builder {
        private var detectOnly: Boolean = true
        private var expectedSigners: List<String> = emptyList()
        private var terminationPolicy: TerminationPolicy = TerminationPolicy.NONE
        private var thresholds: Map<ThreatType, ThreatThreshold> = DefaultThreatThresholds.map
        private var allowDeveloperSignals: Boolean = true
        private var expectedPackageId: String? = null
        private var periodicIntervalSec: Int? = null
        private var detectionTuning: Map<String, SignalTuning> = emptyMap()
        private var allowedInstallSources: Set<String> = DEFAULT_ALLOWED_INSTALL_SOURCES
        private var expectedApkSha256: String? = null
        private var expectedNativeLibSha256: String? = null

        fun detectOnly(value: Boolean) = apply { detectOnly = value }

        fun expectedSigners(value: List<String>) = apply { expectedSigners = value }

        fun terminationPolicy(value: TerminationPolicy) = apply { terminationPolicy = value }

        fun thresholds(value: Map<ThreatType, ThreatThreshold>) = apply { thresholds = value }

        fun allowDeveloperSignals(value: Boolean) = apply { allowDeveloperSignals = value }

        fun expectedPackageId(value: String?) = apply { expectedPackageId = value }

        fun periodicIntervalSec(value: Int?) = apply { periodicIntervalSec = value }

        fun detectionTuning(value: Map<String, SignalTuning>) = apply { detectionTuning = value }

        fun allowedInstallSources(value: Set<String>) = apply { allowedInstallSources = value }

        fun expectedApkSha256(value: String?) = apply { expectedApkSha256 = value }

        fun expectedNativeLibSha256(value: String?) = apply { expectedNativeLibSha256 = value }

        fun build(): MobShieldConfig =
            MobShieldConfig(
                detectOnly = detectOnly,
                expectedSigners = expectedSigners,
                terminationPolicy = terminationPolicy,
                thresholds = thresholds,
                allowDeveloperSignals = allowDeveloperSignals,
                expectedPackageId = expectedPackageId,
                periodicIntervalSec = periodicIntervalSec,
                detectionTuning = detectionTuning,
                allowedInstallSources = allowedInstallSources,
                expectedApkSha256 = expectedApkSha256,
                expectedNativeLibSha256 = expectedNativeLibSha256,
            )
    }

    companion object {
        val DEFAULT_ALLOWED_INSTALL_SOURCES: Set<String> =
            setOf(
                "com.android.vending",
                "com.google.android.packageinstaller",
            )

        fun builder(): Builder = Builder()

        fun validateSigners(signers: List<String>) {
            signers.forEach { validateSha256Hex("expectedSigners", it) }
        }

        fun validateSha256Hex(
            field: String,
            value: String,
        ) {
            val hexPattern = Regex("^[0-9a-fA-F]{64}$")
            require(hexPattern.matches(value)) {
                "$field must be 64-char SHA-256 hex: $value"
            }
        }
    }
}

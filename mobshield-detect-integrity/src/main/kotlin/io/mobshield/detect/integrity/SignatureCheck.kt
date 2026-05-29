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

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import io.mobshield.detect.integrity.internal.DigestUtil
import java.security.MessageDigest

class SignatureCheck(
    private val packageName: String,
    private val expectedSigners: List<String>,
    private val certificateDigestsProvider: () -> List<String>,
) {
    fun scan(): ScanHit? {
        if (expectedSigners.isEmpty()) {
            return null
        }
        val digests = certificateDigestsProvider().map(DigestUtil::normalizeHex).toSet()
        if (digests.isEmpty()) {
            return ScanHit(expected = expectedSigners, actual = emptyList(), reason = "no_certificates")
        }
        val expected = expectedSigners.map(DigestUtil::normalizeHex).toSet()
        val matched = digests.intersect(expected)
        if (matched.isNotEmpty()) {
            return null
        }
        return ScanHit(expected = expectedSigners, actual = digests.toList(), reason = "mismatch")
    }

    data class ScanHit(
        val expected: List<String>,
        val actual: List<String>,
        val reason: String,
    )

    companion object {
        fun certificateDigests(
            packageManager: PackageManager,
            packageName: String,
        ): List<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                certificateDigestsApi28(packageManager, packageName)
            } else {
                certificateDigestsLegacy(packageManager, packageName)
            }
        }

        @SuppressLint("NewApi", "InlinedApi")
        private fun certificateDigestsApi28(
            packageManager: PackageManager,
            packageName: String,
        ): List<String> {
            @Suppress("DEPRECATION")
            val packageInfo =
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
            val signingInfo = packageInfo.signingInfo ?: return emptyList()
            val signatures: Array<Signature> =
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners ?: emptyArray()
                } else {
                    signingInfo.signingCertificateHistory ?: emptyArray()
                }
            return digestSignatures(signatures)
        }

        @Suppress("DEPRECATION")
        private fun certificateDigestsLegacy(
            packageManager: PackageManager,
            packageName: String,
        ): List<String> {
            val packageInfo =
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES,
                )
            val signatures = packageInfo.signatures ?: return emptyList()
            return digestSignatures(signatures)
        }

        private fun digestSignatures(signatures: Array<Signature>): List<String> {
            val digest = MessageDigest.getInstance("SHA-256")
            return signatures.map { signature ->
                DigestUtil.sha256Hex(digest.digest(signature.toByteArray()))
            }
        }
    }
}

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

import android.content.pm.Checksum
import android.content.pm.PackageManager
import android.os.Build
import io.mobshield.detect.integrity.internal.DigestUtil

class ApkChecksumCheck(
    private val expectedApkSha256: String?,
    private val sdkIntProvider: () -> Int = { Build.VERSION.SDK_INT },
    private val checksumProvider: () -> String?,
) {
    fun scan(): ScanHit? {
        val expected = expectedApkSha256 ?: return null
        if (sdkIntProvider() < Build.VERSION_CODES.S) {
            return null
        }
        val actual = checksumProvider() ?: return ScanHit(expected = expected, actual = null, reason = "unavailable")
        if (DigestUtil.hexMatches(expected, actual)) {
            return null
        }
        return ScanHit(expected = expected, actual = actual, reason = "mismatch")
    }

    data class ScanHit(
        val expected: String,
        val actual: String?,
        val reason: String,
    )

    companion object {
        fun requestWholeApkSha256(
            packageManager: PackageManager,
            packageName: String,
        ): String? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return null
            }
            val result = java.util.concurrent.atomic.AtomicReference<String?>()
            val latch = java.util.concurrent.CountDownLatch(1)
            val listener =
                PackageManager.OnChecksumsReadyListener { checksums ->
                    val digest =
                        checksums
                            .firstOrNull { checksum -> checksum.type == Checksum.TYPE_WHOLE_SHA256 }
                            ?.value
                    result.set(digest?.let { DigestUtil.bytesToHex(it) })
                    latch.countDown()
                }
            try {
                packageManager.requestChecksums(
                    packageName,
                    false,
                    Checksum.TYPE_WHOLE_SHA256,
                    emptyList(),
                    listener,
                )
                latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
            } catch (_: Exception) {
                return null
            }
            return result.get()
        }
    }
}

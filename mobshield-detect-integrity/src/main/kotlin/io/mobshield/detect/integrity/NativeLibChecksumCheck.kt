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

import io.mobshield.detect.integrity.internal.DigestUtil
import java.io.File

class NativeLibChecksumCheck(
    private val expectedNativeLibSha256: String?,
    private val libraryFileProvider: () -> File?,
    private val digestComputer: (File) -> String? = { DigestUtil.sha256HexFile(it) },
) {
    fun scan(): ScanHit? {
        val expected = expectedNativeLibSha256 ?: return null
        val file = libraryFileProvider() ?: return ScanHit(expected = expected, actual = null, reason = "missing_file")
        val actual = digestComputer(file) ?: return ScanHit(expected = expected, actual = null, reason = "unreadable")
        if (DigestUtil.hexMatches(expected, actual)) {
            return null
        }
        return ScanHit(expected = expected, actual = actual, reason = "mismatch", path = file.absolutePath)
    }

    data class ScanHit(
        val expected: String,
        val actual: String?,
        val reason: String,
        val path: String? = null,
    )

    companion object {
        const val CORE_LIBRARY_NAME = "libmobshieldcore.so"

        fun coreLibraryFile(nativeLibraryDir: String?): File? {
            if (nativeLibraryDir.isNullOrBlank()) {
                return null
            }
            return File(nativeLibraryDir, CORE_LIBRARY_NAME)
        }
    }
}

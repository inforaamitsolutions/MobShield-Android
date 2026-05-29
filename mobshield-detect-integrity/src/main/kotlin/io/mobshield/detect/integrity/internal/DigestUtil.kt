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
package io.mobshield.detect.integrity.internal

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

internal object DigestUtil {
    fun sha256Hex(bytes: ByteArray): String = formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))

    fun bytesToHex(bytes: ByteArray): String = formatHex(bytes)

    fun sha256HexFile(file: File): String? {
        if (!file.isFile || !file.canRead()) {
            return null
        }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream -> digest.updateStream(stream) }
        return formatHex(digest.digest())
    }

    fun normalizeHex(value: String): String = value.lowercase()

    fun hexMatches(
        expected: String,
        actual: String,
    ): Boolean = normalizeHex(expected) == normalizeHex(actual)

    private fun MessageDigest.updateStream(stream: InputStream) {
        val buffer = ByteArray(8192)
        while (true) {
            val read = stream.read(buffer)
            if (read <= 0) {
                break
            }
            update(buffer, 0, read)
        }
    }

    private fun formatHex(bytes: ByteArray): String {
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}

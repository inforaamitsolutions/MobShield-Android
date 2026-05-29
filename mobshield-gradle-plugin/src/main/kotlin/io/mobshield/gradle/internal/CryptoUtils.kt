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

package io.mobshield.gradle.internal

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object CryptoUtils {
    fun randomBytes(length: Int, seed: ByteArray? = null): ByteArray {
        if (seed != null) {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(seed)
            digest.update("mobshield-entropy-v1".toByteArray())
            val out = ByteArray(length)
            var generated = 0
            var round = 0
            while (generated < length) {
                val block = MessageDigest.getInstance("SHA-256").digest(seed + byteArrayOf(round.toByte()))
                val copy = minOf(block.size, length - generated)
                System.arraycopy(block, 0, out, generated, copy)
                generated += copy
                round++
            }
            return out
        }
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    fun sha256Hex(data: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(data).toHex()

    fun hmacSha256Hex(key: ByteArray, data: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data).toHex()
    }

    fun encryptAesGcm(plaintext: ByteArray, key: ByteArray, salt: ByteArray): AesGcmBlob {
        val aesKey = MessageDigest.getInstance("SHA-256").digest(key + salt).copyOf(32)
        val nonce = randomBytes(12, aesKey + salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"), GCMParameterSpec(128, nonce))
        val ciphertext = cipher.doFinal(plaintext)
        return AesGcmBlob(nonce = nonce, ciphertext = ciphertext)
    }

    fun normalizeSha256(input: String): String = input.replace(":", "").lowercase()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

internal data class AesGcmBlob(val nonce: ByteArray, val ciphertext: ByteArray)

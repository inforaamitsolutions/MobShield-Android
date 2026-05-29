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

import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.util.jar.JarFile

internal object ApkSigningInspector {
    fun readSigningCertSha256(apk: File): String {
        JarFile(apk).use { jar ->
            val certEntry = jar.entries().asSequence().firstOrNull { entry ->
                val name = entry.name
                name.startsWith("META-INF/") &&
                    (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))
            } ?: throw IllegalStateException("No signing block entry found in ${apk.name}")

            jar.getInputStream(certEntry).use { input ->
                val factory = CertificateFactory.getInstance("X.509")
                val certs = factory.generateCertificates(input)
                val cert = certs.firstOrNull()
                    ?: throw IllegalStateException("No certificate in ${certEntry.name}")
                return MessageDigest.getInstance("SHA-256").digest(cert.encoded).toHex()
            }
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

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

package io.mobshield.gradle

import io.mobshield.gradle.internal.ApkSigningInspector
import io.mobshield.gradle.internal.CryptoUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VerifyMobShieldTask : DefaultTask() {
    @get:Input
    abstract val expectedSigningCertSha256: Property<String>

    @get:InputFile
    abstract val mobshieldSeedFile: RegularFileProperty

    @get:InputFile
    abstract val mobshieldHeaderFile: RegularFileProperty

    //@get:Input
    //abstract val apkFiles: Property<Set<File>>

    //@get:Input
    //abstract val nativeLibraryFiles: Property<Set<File>>

    @get:InputFiles
    abstract val apkFiles: org.gradle.api.file.ConfigurableFileCollection

    @get:InputFiles
    abstract val nativeLibraryFiles: org.gradle.api.file.ConfigurableFileCollection

    @TaskAction
    fun run() {
        val expectedSigner = CryptoUtils.normalizeSha256(expectedSigningCertSha256.get())
        if (expectedSigner.isBlank()) {
            throw GradleException("MobShield verify: expectedSigningCertSha256 is not configured")
        }

        val apks = apkFiles.files.filter { it.exists() && it.extension == "apk" }
        if (apks.isEmpty()) {
            throw GradleException("MobShield verify: no APK outputs found. Run assemble first.")
        }

        val apk = apks.maxByOrNull { it.lastModified() }
            ?: throw GradleException("MobShield verify: unable to select APK")

        val actualSigner = ApkSigningInspector.readSigningCertSha256(apk)
        if (actualSigner != expectedSigner) {
            throw GradleException(
                "MobShield verify failed: APK signing cert SHA-256 mismatch.\n" +
                    "Expected: $expectedSigner\n" +
                    "Actual:   $actualSigner\n" +
                    "APK: ${apk.absolutePath}",
            )
        }

        val entropy = readEntropyFromHeader(mobshieldHeaderFile.get().asFile)
        val coreLib = nativeLibraryFiles.files
            .filter { it.name == "libmobshieldcore.so" }
            .maxByOrNull { it.lastModified() }

        if (coreLib == null) {
            logger.warn("MobShield verify: libmobshieldcore.so not found in build outputs; skipping entropy string check")
        } else {
            val libBytes = coreLib.readBytes()
            val marker = "MOBSHIELD_ENTROPY=$entropy".toByteArray()
            if (!containsSubsequence(libBytes, marker)) {
                throw GradleException(
                    "MobShield verify failed: libmobshieldcore.so does not contain MOBSHIELD_BUILD_ENTROPY.\n" +
                        "Expected marker: MOBSHIELD_ENTROPY=$entropy\n" +
                        "Library: ${coreLib.absolutePath}",
                )
            }
        }

        logger.lifecycle("MobShield verify passed for ${apk.name}")
    }

    private fun readEntropyFromHeader(header: File): String {
        val line = header.readLines().firstOrNull { it.contains("MOBSHIELD_BUILD_ENTROPY") }
            ?: throw GradleException("MobShield verify: MOBSHIELD_BUILD_ENTROPY missing in ${header.path}")
        val match = Regex("MOBSHIELD_BUILD_ENTROPY \"([0-9a-fA-F]+)\"").find(line)
            ?: throw GradleException("MobShield verify: unable to parse MOBSHIELD_BUILD_ENTROPY")
        return match.groupValues[1].lowercase()
    }

    private fun containsSubsequence(haystack: ByteArray, needle: ByteArray): Boolean {
        if (needle.isEmpty()) {
            return true
        }
        outer@ for (i in 0..haystack.size - needle.size) {
            for (j in needle.indices) {
                if (haystack[i + j] != needle[j]) {
                    continue@outer
                }
            }
            return true
        }
        return false
    }
}

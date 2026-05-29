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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateMobShieldBuildInfoTask : DefaultTask() {
    @get:Input
    abstract val expectedSigningCertSha256: Property<String>

    @get:Input
    abstract val expectedPackageName: Property<String>

    @get:Input
    abstract val expectedInstallers: ListProperty<String>

    @get:Input
    abstract val aggressive: Property<Boolean>

    @get:Input
    abstract val randomSeed: Property<String>

    @get:Input
    abstract val nativeLibraries: ListProperty<String>

    @get:OutputDirectory
    abstract val mobshieldOutputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val outputDir = mobshieldOutputDir.get().asFile
        val includeDir = File(outputDir, "include")
        val headerFile = File(includeDir, "mobshield_buildinfo.h")
        val seedFile = File(outputDir, "seed")
        val inputsFile = File(outputDir, "inputs.fingerprint")

        val request = BuildInfoRequest(
            expectedSigningCertSha256 = expectedSigningCertSha256.get(),
            expectedPackageName = expectedPackageName.get(),
            expectedInstallers = expectedInstallers.get(),
            aggressive = aggressive.get(),
            randomSeedHex = randomSeed.orNull?.takeIf { it.isNotBlank() },
            nativeLibraries = nativeLibraries.get(),
        )
        val result = BuildInfoGenerator.generate(request)
        if (BuildInfoGenerator.shouldSkipGeneration(inputsFile, headerFile, result.inputsFingerprint)) {
            logger.lifecycle("MobShield: build info up-to-date, skipping generation")
            return
        }

        includeDir.mkdirs()
        headerFile.writeText(result.headerContent)
        seedFile.writeText(result.seedHex)
        inputsFile.writeText(result.inputsFingerprint)
        logger.lifecycle("MobShield: generated ${headerFile.path}")
    }
}

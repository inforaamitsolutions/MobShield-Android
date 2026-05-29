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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class InfoMobShieldTask : DefaultTask() {
    @get:InputFile
    abstract val mobshieldHeaderFile: RegularFileProperty

    @get:InputFile
    abstract val mobshieldSeedFile: RegularFileProperty

    @TaskAction
    fun run() {
        val header = mobshieldHeaderFile.get().asFile
        val seed = mobshieldSeedFile.get().asFile
        logger.lifecycle("MobShield personalization summary")
        logger.lifecycle("  Seed file: ${seed.path}")
        logger.lifecycle("  Header:    ${header.path}")
        header.readLines()
            .filter {
                it.contains("MOBSHIELD_BUILD_ENTROPY") ||
                    it.contains("MOBSHIELD_EXPECTED_SIGNER") ||
                    it.contains("MOBSHIELD_EXPECTED_PACKAGE") ||
                    it.contains("MOBSHIELD_NATIVELIB_HMAC") ||
                    it.startsWith("#define mobshield_")
            }
            .forEach { logger.lifecycle("  $it") }
        logger.lifecycle("  Note: personalized native binaries are not reproducible across builds.")
    }
}

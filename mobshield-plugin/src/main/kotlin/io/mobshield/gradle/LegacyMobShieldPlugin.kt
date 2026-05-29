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

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Legacy plugin id `io.mobshield.gradle`. Prefer `io.mobshield.personalize`. */
class LegacyMobShieldPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target.rootProject.pluginManager.hasPlugin("io.mobshield.personalize")) {
            target.logger.lifecycle("MobShield: io.mobshield.personalize already applied")
            return
        }
        target.pluginManager.apply("io.mobshield.personalize")
    }
}

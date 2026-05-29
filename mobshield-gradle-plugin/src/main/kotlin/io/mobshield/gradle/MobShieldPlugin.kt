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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

class MobShieldPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("io.mobshield.personalize must be applied to an Android application module")
        }

        val extension = project.extensions.create("mobshield", MobShieldExtension::class.java)
        val mobshieldDir = project.layout.buildDirectory.dir("mobshield")
        val includeDir = mobshieldDir.map { it.dir("include") }
        val headerFile = includeDir.map { it.file("mobshield_buildinfo.h") }
        val seedFile = mobshieldDir.map { it.file("seed") }

        val nativeLibraries = listOf(
            "libmobshieldcore.so",
            "libmobshieldroot.so",
            "libmobshieldhooks.so",
            "libmobshielddebug.so",
            "libmobshieldemu.so",
        )

        val generateTask = project.tasks.register(
            "generateMobShieldBuildInfo",
            GenerateMobShieldBuildInfoTask::class.java,
        ) { task ->
            task.group = "mobshield"
            task.description = "Generates mobshield_buildinfo.h and per-build seed"
            task.mobshieldOutputDir.set(mobshieldDir)
            task.expectedSigningCertSha256.set(extension.expectedSigningCertSha256)
            task.expectedPackageName.set(
                project.provider {
                    extension.expectedPackageName.ifBlank {
                        project.extensions.getByType(AppExtension::class.java).defaultConfig.applicationId
                            ?: ""
                    }
                },
            )
            task.expectedInstallers.set(project.provider { extension.expectedInstallers })
            task.aggressive.set(project.provider { extension.aggressive })
            task.randomSeed.set(project.provider { extension.randomSeed ?: "" })
            task.nativeLibraries.set(nativeLibraries)
            task.outputs.upToDateWhen {
                val output = mobshieldDir.get().asFile
                val inputs = File(output, "inputs.fingerprint")
                val header = File(File(output, "include"), "mobshield_buildinfo.h")
                inputs.exists() && header.exists()
            }
        }

        project.tasks.named("preBuild").configure { task -> task.dependsOn(generateTask) }

        generateTask.configure { task ->
            task.doLast {
                syncGeneratedHeaderToNativeModules(project.rootProject, headerFile.get().asFile)
            }
        }

        wireMobshieldNativeModules(project, includeDir, generateTask)
        project.rootProject.extensions.extraProperties.set(
            "mobshield.generatedIncludeDir",
            includeDir.get().asFile.absolutePath,
        )

        val verifyTask = project.tasks.register("mobshieldVerify", VerifyMobShieldTask::class.java) { task ->
            task.group = "mobshield"
            task.description = "Verifies APK signing cert and MobShield entropy in native libraries"
            task.expectedSigningCertSha256.set(extension.expectedSigningCertSha256)
            task.mobshieldSeedFile.set(seedFile)
            task.mobshieldHeaderFile.set(headerFile)
            task.apkFiles.from(project.provider { collectApkOutputs(project) })
            task.nativeLibraryFiles.from(project.provider { collectNativeLibraries(project) })
        }

        project.tasks.named("assemble").configure { task -> task.finalizedBy(verifyTask) }
        verifyTask.configure { task ->
            task.onlyIf {
                val normalized = extension.expectedSigningCertSha256.replace(":", "").trim()
                normalized.isNotEmpty() && !normalized.all { it == '0' }
            }
        }

        project.tasks.register("mobshieldInfo", InfoMobShieldTask::class.java) { task ->
            task.group = "mobshield"
            task.description = "Prints MobShield personalization summary"
            task.mobshieldHeaderFile.set(headerFile)
            task.mobshieldSeedFile.set(seedFile)
            task.dependsOn(generateTask)
        }

        project.logger.lifecycle("MobShield personalize plugin applied to ${project.path}")
    }

    private fun wireMobshieldNativeModules(
        appProject: Project,
        includeDir: Provider<Directory>,
        generateTask: org.gradle.api.tasks.TaskProvider<GenerateMobShieldBuildInfoTask>,
    ) {
        val root = appProject.rootProject
        root.gradle.projectsEvaluated {
            val includePath = includeDir.get().asFile.absolutePath
            root.subprojects { sub ->
                if (!sub.path.startsWith(":mobshield")) {
                    return@subprojects
                }
                sub.plugins.withId("com.android.library") {
                    sub.extensions.findByType(LibraryExtension::class.java)?.let { androidExt ->
                        androidExt.defaultConfig.externalNativeBuild.cmake.arguments(
                            "-DMOBSHIELD_GENERATED_INCLUDE_DIR=$includePath",
                        )
                        if (sub.path.contains("hooks")) {
                            androidExt.defaultConfig.externalNativeBuild.cmake.arguments(
                                "-DMOBSHIELD_HOOKS_GENERATED_INCLUDE_DIR=$includePath",
                            )
                        }
                        sub.tasks.matching {
                            it.name == "generateMobShieldBuildInfo" || it.name == "generateHooksBuildInfo"
                        }.configureEach { task -> task.setEnabled(false) }
                        sub.tasks.matching { it.name.contains("externalNativeBuild", ignoreCase = true) }
                            .configureEach { task -> task.dependsOn(generateTask) }
                    }
                }
            }
        }
    }

    private fun syncGeneratedHeaderToNativeModules(root: Project, header: File) {
        if (!header.exists()) {
            return
        }
        root.subprojects { sub ->
            if (!sub.path.startsWith(":mobshield")) {
                return@subprojects
            }
            val destDir = sub.layout.buildDirectory.dir("generated/mobshield/include").get().asFile
            destDir.mkdirs()
            header.copyTo(File(destDir, "mobshield_buildinfo.h"), overwrite = true)
        }
    }

    private fun collectApkOutputs(project: Project): Set<File> {
        val outputs = mutableSetOf<File>()
        val apkDir = project.layout.buildDirectory.dir("outputs/apk").get().asFile
        if (apkDir.exists()) {
            apkDir.walkTopDown().filter { it.isFile && it.extension == "apk" }.forEach { outputs.add(it) }
        }
        return outputs
    }

    private fun collectNativeLibraries(project: Project): Set<File> {
        val libs = mutableSetOf<File>()
        project.rootProject.subprojects { sub ->
            val cxxDir = sub.layout.buildDirectory.dir("intermediates/cxx").get().asFile
            if (cxxDir.exists()) {
                cxxDir.walkTopDown()
                    .filter { it.isFile && it.name.startsWith("libmobshield") && it.extension == "so" }
                    .forEach { libs.add(it) }
            }
        }
        return libs
    }
}

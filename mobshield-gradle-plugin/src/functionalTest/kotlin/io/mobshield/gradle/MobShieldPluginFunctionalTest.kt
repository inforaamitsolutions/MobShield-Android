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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.Properties

class MobShieldPluginFunctionalTest {
    @Test
    fun generateMobShieldBuildInfo_writesPersonalizedHeader() {
        val projectDir = File("src/functionalTest/resources/stub-android")
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withGradleVersion("8.7")
            .withArguments(":app:generateMobShieldBuildInfo", "--stacktrace")
            .forwardOutput()
            .build()

        assertSuccessful(result.task(":app:generateMobShieldBuildInfo")!!.outcome)

        val header = File(projectDir, "app/build/mobshield/include/mobshield_buildinfo.h")
        assertTrue("Expected generated header at ${header.path}", header.exists())
        val content = header.readText()
        assertTrue(content.contains("MOBSHIELD_BUILD_ENTROPY"))
        assertTrue(content.contains("MOBSHIELD_R_"))
        assertTrue(content.contains("Not reproducible across builds"))
    }

    @Test
    fun assembleDebug_succeedsOnStubApp() {
        val sdkDir = resolveAndroidSdkDir()
        assumeTrue("Android SDK not configured (set ANDROID_HOME or local.properties sdk.dir)", sdkDir != null)

        val projectDir = File("src/functionalTest/resources/stub-android")
        val localProperties = File(projectDir, "local.properties")
        localProperties.writeText("sdk.dir=${sdkDir!!.replace(":", "\\:")}\n")
        try {
            val result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withGradleVersion("8.7")
                .withArguments(":app:assembleDebug", "--stacktrace")
                .forwardOutput()
                .build()

            assertSuccessful(result.task(":app:assembleDebug")!!.outcome)
        } finally {
            localProperties.delete()
        }
    }

    private fun assertSuccessful(outcome: TaskOutcome) {
        assertTrue(
            "Expected task SUCCESS or UP_TO_DATE but was $outcome",
            outcome == TaskOutcome.SUCCESS || outcome == TaskOutcome.UP_TO_DATE,
        )
    }

    private fun resolveAndroidSdkDir(): String? {
        System.getenv("ANDROID_HOME")?.takeIf { File(it).isDirectory }?.let { return it }
        System.getenv("ANDROID_SDK_ROOT")?.takeIf { File(it).isDirectory }?.let { return it }
        val candidates = listOf(
            File("local.properties"),
            File("../local.properties"),
            File("../../local.properties"),
        )
        for (file in candidates) {
            if (!file.exists()) {
                continue
            }
            val props = Properties()
            file.inputStream().use { props.load(it) }
            val sdk = props.getProperty("sdk.dir") ?: continue
            if (File(sdk).isDirectory) {
                return sdk
            }
        }
        return null
    }
}

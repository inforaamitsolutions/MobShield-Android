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

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.Test
import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.0.21"
    `jvm-test-suite`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = findProperty("GROUP") as String? ?: "io.mobshield"
version =
    findProperty("VERSION_NAME") as String?
        ?: findProperty("mobshield.version") as String?
        ?: "0.0.0-SNAPSHOT"

gradlePlugin {
    plugins {
        register("mobshieldPersonalize") {
            id = "io.mobshield.personalize"
            implementationClass = "io.mobshield.gradle.MobShieldPlugin"
            displayName = "MobShield Personalize"
            description = "Per-build MobShield native personalization for Android apps"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(gradleApi())
    compileOnly("com.android.tools.build:gradle:8.5.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useJUnit()
            dependencies {
                implementation(project())
                implementation(gradleTestKit())
                implementation("junit:junit:4.13.2")
                runtimeOnly(files(tasks.pluginUnderTestMetadata))
            }
            sources {
                kotlin {
                    setSrcDirs(listOf("src/functionalTest/kotlin"))
                }
                resources {
                    setSrcDirs(listOf("src/functionalTest/resources"))
                }
            }
            targets {
                all {
                    testTask.configure {
                        description = "Gradle TestKit functional tests for io.mobshield.personalize"
                        dependsOn(tasks.jar, tasks.pluginUnderTestMetadata)
                    }
                }
            }
        }
    }
}

tasks.named<Test>("test") {
    useJUnit()
}

tasks.named("check") {
    dependsOn(testing.suites.named<JvmTestSuite>("functionalTest"))
}

extensions.configure<MavenPublishBaseExtension> {
    coordinates(group.toString(), "mobshield-gradle-plugin", version.toString())
    pom {
        name.set("MobShield Gradle Personalize Plugin")
        description.set("Per-build MobShield native personalization for Android applications")
        inceptionYear.set("2025")
        url.set("https://github.com/mobshield/mobshield")
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
            license {
                name.set("Business Source License 1.1")
                url.set("https://github.com/mobshield/mobshield/blob/main/mobshield-android/LICENSE-BSL")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("mobshield")
                name.set("MobShield Contributors")
                email.set("security@mobshield.dev")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/mobshield/mobshield.git")
            developerConnection.set("scm:git:ssh://git@github.com/mobshield/mobshield.git")
            url.set("https://github.com/mobshield/mobshield/tree/main")
        }
    }
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    val signingConfigured =
        listOf("signingInMemoryKey", "signing.keyId", "SIGNING_KEY").any {
            findProperty(it) != null || System.getenv(it) != null
        }
    if (signingConfigured) {
        signAllPublications()
    }
}

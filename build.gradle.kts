import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.7" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.nexus.publish)
}

val publishableAndroidModules =
    setOf(
        "mobshield",
        "mobshield-core",
        "mobshield-detect-root",
        "mobshield-detect-hooks",
        "mobshield-detect-debugger",
        "mobshield-detect-environment",
        "mobshield-detect-integrity",
    )

// Detekt infers JVM target from the running JDK; pin to project target so JDK 21+ local installs still build.
subprojects {
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "17"
    }
}

// JitPack: set ORG_GRADLE_PROJECT_GROUP in jitpack.yml to com.github.<user>.<repo>
// so transitive module coordinates resolve from the same JitPack publication.
val mobshieldGroup: String = findProperty("GROUP") as String? ?: "io.mobshield"
val mobshieldVersion: String =
    // Prefer explicit Gradle property (used by Maven Central CI).
    findProperty("VERSION_NAME") as String?
        // Fall back to repo-local default.
        ?: findProperty("mobshield.version") as String?
        // Allow JitPack to inject a version from its VERSION env var (e.g. tag).
        ?: System.getenv("VERSION")?.removePrefix("v")
        // Final fallback for local development.
        ?: "0.0.0-SNAPSHOT"

subprojects {
    if (name !in publishableAndroidModules) {
        return@subprojects
    }

    pluginManager.apply(rootProject.libs.plugins.maven.publish.get().pluginId)

    val artifactId = name
    group = mobshieldGroup
    version = mobshieldVersion

    extensions.configure<MavenPublishBaseExtension> {
        coordinates(mobshieldGroup, artifactId, mobshieldVersion)

        pom {
            name.set("MobShield $artifactId")
            description.set("MobShield mobile RASP library: $artifactId")
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
        if (hasSigningCredentials()) {
            signAllPublications()
        }
    }
}

fun hasSigningCredentials(): Boolean {
    val props = listOf(
        "signingInMemoryKey",
        "signing.keyId",
        "SIGNING_KEY",
    )
    return props.any { rootProject.findProperty(it) != null || System.getenv(it) != null }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

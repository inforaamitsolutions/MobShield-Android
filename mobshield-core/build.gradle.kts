plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

val mobshieldVersion: String =
    findProperty("VERSION_NAME") as String?
        ?: findProperty("mobshield.version") as String?
        ?: "0.1.0"
val defaultGeneratedIncludeDir = layout.buildDirectory.dir("generated/mobshield/include").get().asFile

tasks.register("generateMobShieldBuildInfo") {
    val outputDir = defaultGeneratedIncludeDir
    outputs.dir(outputDir)
    doLast {
        outputDir.mkdirs()
        val buildId = "android-$mobshieldVersion-${System.currentTimeMillis()}"
        val header =
            """
            |#ifndef MOBSHIELD_BUILDINFO_H
            |#define MOBSHIELD_BUILDINFO_H
            |#define MOBSHIELD_VERSION "$mobshieldVersion"
            |#define MOBSHIELD_BUILD_ID "$buildId"
            |#define MOBSHIELD_SELF_CHECK_MAGIC 0x4D534844u
            |#endif
            """.trimMargin()
        File(outputDir, "mobshield_buildinfo.h").writeText(header)
    }
}

android {
    namespace = "io.mobshield.core"
    compileSdk = libs.versions.compileSdk.get().toInt()
    ndkVersion = "26.3.11579264"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-Wall", "-Wextra")
                arguments +=
                    listOf(
                        "-DMOBSHIELD_GENERATED_INCLUDE_DIR=${defaultGeneratedIncludeDir.absolutePath}",
                    )
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.named("preBuild").configure {
    dependsOn("generateMobShieldBuildInfo")
}

tasks.matching { task ->
    task.name.startsWith("configureCMake") || task.name.startsWith("buildCMake")
}.configureEach {
    dependsOn("generateMobShieldBuildInfo")
}

gradle.projectsEvaluated {
    val extra = rootProject.extensions.extraProperties
    if (extra.has("mobshield.generatedIncludeDir")) {
        tasks.named("generateMobShieldBuildInfo").configure { enabled = false }
    }
}

dependencies {
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

detekt {
    config.setFrom(files("${rootProject.projectDir}/detekt.yml"))
}

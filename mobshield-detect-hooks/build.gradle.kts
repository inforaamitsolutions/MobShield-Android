plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

val generatedIncludeDir = layout.buildDirectory.dir("generated/mobshield/hooks/include")

tasks.register("generateHooksBuildInfo") {
    val outputDir = generatedIncludeDir.get().asFile
    outputs.dir(outputDir)
    doLast {
        outputDir.mkdirs()
        val seedHigh = (System.nanoTime() and 0xFFFFFFFF).toInt()
        val seedLow = (System.currentTimeMillis() and 0xFFFFFFFF).toInt()
        val header =
            buildString {
                appendLine("#ifndef MOBSHIELD_HOOKS_BUILDINFO_H")
                appendLine("#define MOBSHIELD_HOOKS_BUILDINFO_H")
                appendLine("#define MOBSHIELD_HOOKS_ENTROPY_SEED_HIGH 0x${seedHigh.toUInt().toString(16)}u")
                appendLine("#define MOBSHIELD_HOOKS_ENTROPY_SEED_LOW 0x${seedLow.toUInt().toString(16)}u")
                appendLine("#define MOBSHIELD_PROLOGUE_BYTES 16")
                appendLine("#define MOBSHIELD_PROLOGUE_FN_COUNT 4")
                appendLine(
                    "// Encrypted baselines (unset 0xFF XOR key). Personalized builds replace via mobshield-plugin.",
                )
                appendLine("static const unsigned char kEncryptedPrologueOpen[16] = {")
                append(encryptBytesLine(16, seedHigh, seedLow, 0))
                appendLine("static const unsigned char kEncryptedPrologueRead[16] = {")
                append(encryptBytesLine(16, seedHigh, seedLow, 1))
                appendLine("static const unsigned char kEncryptedPrologueSyscall[16] = {")
                append(encryptBytesLine(16, seedHigh, seedLow, 2))
                appendLine("static const unsigned char kEncryptedPrologueDlopen[16] = {")
                append(encryptBytesLine(16, seedHigh, seedLow, 3))
                appendLine("#endif")
            }
        File(outputDir, "mobshield_hooks_buildinfo.h").writeText(header)
    }
}

fun encryptBytesLine(
    count: Int,
    seedHigh: Int,
    seedLow: Int,
    fnIndex: Int,
): String {
    val bytes = ByteArray(count) { 0xFF.toByte() }
    val key = deriveKey(seedHigh, seedLow, fnIndex)
    val encrypted = bytes.mapIndexed { index, value -> (value.toInt() xor key[index % key.size]) and 0xFF }
    return "    " + encrypted.joinToString(", ") { "0x%02x".format(it) } + "\n};\n"
}

fun deriveKey(
    seedHigh: Int,
    seedLow: Int,
    fnIndex: Int,
): IntArray {
    var state = seedHigh xor seedLow xor (fnIndex * 0x9E3779B9.toInt())
    return IntArray(16) {
        state = state xor (state shl 13)
        state = state xor (state ushr 17)
        state = state xor (state shl 5)
        state and 0xFF
    }
}

android {
    namespace = "io.mobshield.detect.hooks"
    compileSdk = libs.versions.compileSdk.get().toInt()
    ndkVersion = libs.versions.ndk.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-Wall", "-Wextra")
                arguments +=
                    listOf(
                        "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON",
                        "-DMOBSHIELD_HOOKS_GENERATED_INCLUDE_DIR=${generatedIncludeDir.get().asFile.absolutePath}",
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.named("preBuild").configure {
    dependsOn("generateHooksBuildInfo")
}

tasks.matching { task ->
    task.name.startsWith("configureCMake") || task.name.startsWith("buildCMake")
}.configureEach {
    dependsOn("generateHooksBuildInfo")
}

dependencies {
    api(project(":mobshield-core"))
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

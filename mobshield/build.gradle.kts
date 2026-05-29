plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.mobshield"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(project(":mobshield-core"))
    api(project(":mobshield-detect-root"))
    api(project(":mobshield-detect-hooks"))
    api(project(":mobshield-detect-debugger"))
    api(project(":mobshield-detect-environment"))
    api(project(":mobshield-detect-integrity"))
}

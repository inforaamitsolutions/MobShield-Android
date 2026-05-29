plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    id("io.mobshield.personalize")
}

mobshield {
    expectedSigningCertSha256 = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"
    expectedPackageName = "io.mobshield.sample"
    expectedInstallers = listOf("com.android.vending")
    aggressive = false
}

android {
    namespace = "io.mobshield.sample"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.mobshield.sample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
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
    implementation(project(":mobshield"))
}

detekt {
    config.setFrom(files("${rootProject.projectDir}/detekt.yml"))
}

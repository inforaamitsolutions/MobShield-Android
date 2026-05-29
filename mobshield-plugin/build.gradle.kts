plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
}

gradlePlugin {
    plugins {
        register("mobshield") {
            id = "io.mobshield.gradle"
            implementationClass = "io.mobshield.gradle.LegacyMobShieldPlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(gradleApi())
}

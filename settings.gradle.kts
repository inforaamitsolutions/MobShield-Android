pluginManagement {
    includeBuild("mobshield-gradle-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mobshield-android"

include(":mobshield")
include(":mobshield-core")
include(":mobshield-detect-root")
include(":mobshield-detect-hooks")
include(":mobshield-detect-debugger")
include(":mobshield-detect-environment")
include(":mobshield-detect-integrity")
include(":mobshield-plugin")
include(":mobshield-sample-app")

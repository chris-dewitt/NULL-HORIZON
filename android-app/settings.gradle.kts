pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Auto-provisions the JDK 17 required by the Kotlin toolchain when the machine
// only has a newer JDK installed. Without this, the Kotlin compiler can end up
// on the launcher JDK (e.g. JDK 25, which Kotlin 2.0.x cannot parse).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "null-horizon"
include(":app")

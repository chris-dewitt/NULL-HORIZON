pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Auto-provisions the JDK 17 required by jvmToolchain(17) when the machine
// only has a newer JDK installed. Without this, the Kotlin daemon can end up
// on the launcher JDK (e.g. JDK 25, which Kotlin 2.0.x cannot parse).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "null-horizon-pc"

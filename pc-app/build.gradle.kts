import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
}

group = "com.nullhorizon"
version = "0.1.0-pc"

kotlin {
    jvmToolchain(17)

    // Cross-client game core shared with android-app (see ADR-0020). Sources are
    // compiled by each client's own toolchain; no separate module or publishing.
    sourceSets {
        named("main") {
            kotlin.srcDir("../shared/client-core/src/main/kotlin")
            resources.srcDir("../shared/client-core/src/main/resources")
        }
        named("test") {
            kotlin.srcDir("../shared/client-core/src/test/kotlin")
            resources.srcDir("../shared/client-core/src/main/resources")
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
    // Mission SQL console: isolated in-memory SQLite via JDBC (same policy as Android ADR-0007).
    implementation(libs.sqlite.jdbc)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}

compose.desktop {
    application {
        mainClass = "com.nullhorizon.pc.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NULL HORIZON"
            packageVersion = "1.0.0"
            description = "NULL HORIZON PC client — narrative backend-learning game"
            copyright = "Copyright © NULL HORIZON contributors"
            vendor = "NULL HORIZON"
        }
    }
}

tasks.test {
    useJUnit()
}

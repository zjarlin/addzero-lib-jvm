plugins {
    id("kmp-convention")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
        }
    }
}
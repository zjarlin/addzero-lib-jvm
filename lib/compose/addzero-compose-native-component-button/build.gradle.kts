plugins {
    id("kmp-component")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.compose.addzeroComposeNativeComponentHighLevel)
            }
        }
    }
}

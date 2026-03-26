plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.compose.composeNativeComponentHighLevel)
            }
        }
    }
}

plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
    id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.compose.composeNativeComponentButton)
                implementation(projects.lib.compose.composeNativeComponentSearchbar)
            }
        }
    }
}

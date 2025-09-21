plugins {
    id("kmp-component")
    id("kmp-json-withtool")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.compose.addzeroComposeNativeComponentButton)
                implementation(projects.lib.toolKmp.addzeroTool)
                implementation(projects.lib.compose.addzeroComposeNativeComponentSearchbar)
//                implementation(projects.lib.toolKmp.addzeroToolJson)

            }
        }
    }
}

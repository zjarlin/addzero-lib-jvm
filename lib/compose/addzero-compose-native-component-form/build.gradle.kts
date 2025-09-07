plugins {
    id("kmp-component")
}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lib.toolKmp.addzeroTool)
                implementation(projects.lib.compose.addzeroComposeNativeComponentButton)
                implementation(projects.lib.compose.addzeroComposeNativeComponentTree)
            }
        }


    }
}

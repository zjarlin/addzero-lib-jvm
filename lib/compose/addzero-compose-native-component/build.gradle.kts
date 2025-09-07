plugins {
    id("kmp-component")
    id("kmp-json-withtool")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
            implementation(projects.lib.toolKmp.addzeroTool)


        }
    }
}

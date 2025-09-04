plugins {
    id("kmp-component")
    id("kmp-json")
//    id("kmp-koin")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
        }
    }
}

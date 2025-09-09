plugins {
    id("kmp-ktor")
//    id("kmp-koin")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
        }


    }
}

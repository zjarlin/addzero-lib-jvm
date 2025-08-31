plugins {
    id("kmp-ktor")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
        }


    }
}

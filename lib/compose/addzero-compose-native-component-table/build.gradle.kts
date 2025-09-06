
plugins {
    id("kmp-component")
    id("kmp-json")
//    id("kmp-koin")
    id("ksp4self")
}
dependencies {
    kspCommonMainMetadata(projects.lib.compose.addzeroComposePropsProcessor)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
            implementation(projects.lib.compose.addzeroComposePropsAnnotations)
        }
    }
}

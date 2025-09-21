
plugins {
    id("kmp-component")
    id("kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(projects.lib.compose.addzeroComposePropsProcessor)
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.compose.addzeroComposeNativeComponentCard)
//            implementation(projects.lib.compose.addzeroComposeNativeComponent)
//            implementation(projects.lib.toolKmp.addzeroToolJson)
            implementation(projects.lib.compose.addzeroComposePropsAnnotations)
           api (projects.lib.compose.addzeroComposeNativeComponentTableCore)
        }
    }
}

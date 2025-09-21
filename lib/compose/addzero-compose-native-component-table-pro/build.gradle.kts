
plugins {
    id("kmp-component")
    id("kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(projects.lib.compose.addzeroComposePropsProcessor)
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(projects.lib.toolKmp.addzeroToolJson)
            implementation(projects.lib.compose.addzeroComposePropsAnnotations)
//            implementation(projects.lib.compose.addzerosearch)
            api(projects.lib.compose.addzeroComposeNativeComponentTable)
            implementation(projects.lib.compose.addzeroComposeNativeComponentButton)
            implementation(projects.lib.compose.addzeroComposeNativeComponentSearchbar)
            implementation(projects.lib.compose.addzeroComposeNativeComponentAssist)
            implementation(projects.lib.compose.addzeroComposeNativeComponentSelect)
            implementation(projects.lib.compose.addzeroComposeNativeComponentHighLevel)
            implementation(projects.lib.compose.addzeroComposeNativeComponentForm)
            implementation(projects.lib.compose.addzeroComposeNativeComponentTree)
            implementation(projects.lib.compose.addzeroComposeNativeComponentCard)
//            implementation(projects.lib.toolKmp.addzeroToolStr)
            implementation(projects.lib.toolKmp.addzeroTool)
        }
    }
}


plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
    id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(projects.lib.compose.composePropsProcessor)
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(projects.lib.toolKmp.toolJson)
            implementation("site.addzero:compose-props-annotations:2025.09.30")
//            implementation(projects.lib.compose.addzerosearch)
            api(projects.lib.compose.composeNativeComponentTable)
            implementation(projects.lib.compose.composeNativeComponentButton)
            implementation(projects.lib.compose.composeNativeComponentSearchbar)
            implementation(projects.lib.compose.composeNativeComponentAssist)
            implementation(projects.lib.compose.composeNativeComponentSelect)
            implementation(projects.lib.compose.composeNativeComponentHighLevel)
            implementation(projects.lib.compose.composeNativeComponentForm)
            implementation(projects.lib.compose.composeNativeComponentTree)
            implementation(projects.lib.compose.composeNativeComponentCard)
//            implementation(projects.lib.toolKmp.toolStr)
//            implementation(projects.lib.toolKmp.tool)
        }
    }
}

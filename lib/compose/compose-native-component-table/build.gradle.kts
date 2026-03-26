
plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
    id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(projects.lib.compose.composePropsProcessor)
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.compose.composeNativeComponentCard)
//            implementation(projects.lib.compose.composeNativeComponent)
//            implementation(projects.lib.toolKmp.toolJson)
            implementation("site.addzero:compose-props-annotations:2025.09.30")
           api (projects.lib.compose.composeNativeComponentTableCore)
        }
    }
}

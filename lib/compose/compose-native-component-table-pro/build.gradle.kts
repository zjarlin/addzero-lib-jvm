
plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(project(":lib:compose:compose-props-processor"))
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(project(":lib:tool-kmp:tool-json"))
            implementation("site.addzero:compose-props-annotations:2025.09.30")
//            implementation(projects.lib.compose.addzerosearch)
            api(project(":lib:compose:compose-crud-spi"))
            api(project(":lib:compose:compose-native-component-table"))
            implementation(project(":lib:compose:compose-native-component-button"))
            implementation(project(":lib:compose:compose-native-component-searchbar"))
            implementation(project(":lib:compose:compose-native-component-assist"))
            implementation(project(":lib:compose:compose-native-component-select"))
            implementation(project(":lib:compose:compose-native-component-high-level"))
            implementation(project(":lib:compose:compose-native-component-form"))
            implementation(project(":lib:compose:compose-native-component-tree"))
            implementation(project(":lib:compose:compose-native-component-card"))
//            implementation(project(":lib:tool-kmp:tool-str"))
//            implementation(projects.lib.toolKmp.tool)
        }
    }
}

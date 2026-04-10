
plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

//dependencies {
//    kspCommonMainMetadata(project(":lib:compose:compose-props-processor"))
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("site.addzero:tool-json:2026.02.04")
            implementation(libs.findLibrary("site-addzero-compose-props-annotations").get())
//            implementation(projects.lib.compose.addzerosearch)
            api(project(":lib:compose:compose-crud-spi"))
            api(libs.findLibrary("site-addzero-compose-native-component-table").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-assist").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-select").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-high-level").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-form").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-card").get())
//            implementation("site.addzero:tool-str:2026.04.10")
//            implementation(projects.lib.toolKmp.tool)
        }
    }
}

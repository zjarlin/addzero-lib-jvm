package site.addzero.buildlogic.conventions

plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-compose-native-component").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-assist").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-card").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-form").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-high-level").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-hook").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-select").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-table").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-table-core").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-table-pro").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-hook").get())
        implementation(libs.findLibrary("site-addzero-compose-klibs-component").get())
    }
}

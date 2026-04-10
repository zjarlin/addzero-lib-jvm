plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-workbench-design").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
        }
    }
}

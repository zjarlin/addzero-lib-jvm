plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val libs = versionCatalogs.named("libs")


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation()



    implementation(libs.findLibrary("site-addzero-compose-native-component-autocomplet").get())

    implementation(libs.findLibrary("site-addzero-compose-native-component-text").get())

    implementation(project(":lib:compose:compose-native-component-toast"))
}
    }
}

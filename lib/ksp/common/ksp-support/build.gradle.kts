plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(libs.findLibrary("site-addzero-tool-json").get())
            api(libs.findLibrary("site-addzero-tool-str").get())
        }
        jvmMain.dependencies {
            implementation(libs.findLibrary("com-belerweb-pinyin4j").get())
            implementation(libs.findLibrary("site-addzero-tool-io-codegen").get())
        }
    }
}

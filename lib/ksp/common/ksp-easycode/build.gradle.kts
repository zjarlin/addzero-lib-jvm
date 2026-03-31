plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val libs = versionCatalogs.named("libs")


kotlin {
    dependencies {
        implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
        implementation(libs.findLibrary("site-addzero-tool-io-codegen").get())
        implementation(libs.findLibrary("site-addzero-tool-str").get())
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("org-apache-velocity-velocity-engine-core").get())
        }
    }
}

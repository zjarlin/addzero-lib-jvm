plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val libs = versionCatalogs.named("libs")


kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-ksp-support").get())

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("org-apache-velocity-velocity-engine-core").get())
        }
    }

}

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}
val libs = versionCatalogs.named("libs")

dependencies {

    api(libs.findLibrary("site-addzero-lsi-core").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())
}

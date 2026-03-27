plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")  version "2025.12.20"
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("site-addzero-tool-curl").get())
    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())
    implementation(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
}

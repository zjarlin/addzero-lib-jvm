plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    api(libs.findLibrary("androidx-room-compiler-processing").get())
    api(libs.findLibrary("site-addzero-lsi-core").get())
}

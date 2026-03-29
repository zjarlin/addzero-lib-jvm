plugins {
    id("site.addzero.buildlogic.jvm.jvm-json")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}

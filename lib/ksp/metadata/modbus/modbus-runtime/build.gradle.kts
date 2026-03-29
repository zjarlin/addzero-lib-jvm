plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    alias(libs.plugins.kotlinSerialization)
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("j2mod").get())
    implementation(libs.findLibrary("jserialcomm").get())
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
}

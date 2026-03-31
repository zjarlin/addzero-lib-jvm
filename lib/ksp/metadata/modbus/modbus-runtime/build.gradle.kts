plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    alias(libs.plugins.kotlinSerialization)
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:tool-jvm:tool-modbus"))
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

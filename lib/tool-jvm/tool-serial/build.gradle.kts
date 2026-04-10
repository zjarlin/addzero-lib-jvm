plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("jserialcomm").get())
    api(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

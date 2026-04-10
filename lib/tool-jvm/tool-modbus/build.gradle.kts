plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("j2mod").get())
    api(libs.findLibrary("tool-serial").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

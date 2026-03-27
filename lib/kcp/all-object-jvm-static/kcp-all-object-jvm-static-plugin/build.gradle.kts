plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

group = "site.addzero"

dependencies {
    compileOnly(libs.findLibrary("org-jetbrains-kotlin-kotlin-compiler-embeddable").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-compiler-embeddable").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

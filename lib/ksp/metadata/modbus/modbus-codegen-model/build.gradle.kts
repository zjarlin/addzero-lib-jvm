plugins {
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
}

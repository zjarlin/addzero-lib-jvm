plugins {
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("modbus-codegen-model").get())
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
}

plugins {
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

val libs = versionCatalogs.named("libs")

dependencies {
    api(project(":lib:ksp:metadata:modbus:modbus-codegen-model"))
    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
}

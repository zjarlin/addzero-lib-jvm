plugins {
    id("site.addzero.buildlogic.jvm.jvm-json")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    testImplementation(project(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway"))
    testImplementation(project(":lib:ksp:metadata:modbus:modbus-ksp-c-contract"))
    testImplementation(project(":lib:ksp:metadata:modbus:modbus-ksp-markdown"))
}

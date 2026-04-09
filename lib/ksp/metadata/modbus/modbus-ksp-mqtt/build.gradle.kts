plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-core"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-c-contract"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-keil-sync"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-markdown"))
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}

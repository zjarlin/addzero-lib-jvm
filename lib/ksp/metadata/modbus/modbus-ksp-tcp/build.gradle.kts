plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("modbus-ksp-core").get())
    implementation(libs.findLibrary("modbus-ksp-kotlin-gateway").get())
    implementation(libs.findLibrary("modbus-ksp-c-contract").get())
    implementation(libs.findLibrary("modbus-ksp-keil-sync").get())
    implementation(libs.findLibrary("modbus-ksp-markdown").get())
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}

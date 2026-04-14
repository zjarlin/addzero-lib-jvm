plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(projects.lib.ksp.metadata.modbus.modbusKspCore)
    implementation(projects.lib.ksp.metadata.modbus.modbusKspKotlinGateway)
    implementation(libs.findLibrary("modbus-ksp-c-contract").get())
    implementation(libs.findLibrary("modbus-ksp-keil-sync").get())
    implementation(libs.findLibrary("modbus-ksp-markdown").get())
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}

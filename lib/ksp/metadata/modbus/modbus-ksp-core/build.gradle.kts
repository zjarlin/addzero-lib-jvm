plugins {
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    testImplementation(libs.findLibrary("modbus-ksp-kotlin-gateway").get())
    testImplementation(libs.findLibrary("modbus-ksp-c-contract").get())
    testImplementation(libs.findLibrary("modbus-ksp-markdown").get())
    testImplementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
}

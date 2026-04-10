plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("modbus-ksp-core").get())
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())

    testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

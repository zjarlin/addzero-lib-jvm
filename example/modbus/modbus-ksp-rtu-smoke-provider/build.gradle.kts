plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    compileOnly(projects.lib.ksp.metadata.modbus.modbusKsp)
    compileOnly(catalogLibs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    implementation(catalogLibs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
}

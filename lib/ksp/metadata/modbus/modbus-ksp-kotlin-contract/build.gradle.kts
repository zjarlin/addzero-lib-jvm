plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(projects.lib.ksp.metadata.modbus.modbusCodegenCore)
    implementation(projects.lib.ksp.metadata.modbus.modbusKspCore)
}

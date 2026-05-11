plugins {
  id("site.addzero.buildlogic.jvm.jvm-json-withtool")
  alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}

val libs = versionCatalogs.named("libs")

processorBuddy {
  packageName.set("site.addzero.device.protocol.modbus.ksp.context")
  mustMap.set(
    mapOf(
      "addzero.modbus.transports" to """listOf("rtu")""",
      "addzero.modbus.codegen.mode" to """listOf("server")""",
      "addzero.modbus.contractPackages" to ",",
      "addzero.modbus.contractPackage" to "",
      "addzero.modbus.metadata.providers" to ",",
      "addzero.modbus.database.driverClass" to "",
      "addzero.modbus.database.jdbcUrl" to "",
      "addzero.modbus.database.username" to "",
      "addzero.modbus.database.password" to "",
      "addzero.modbus.database.query" to "",
      "addzero.modbus.database.jsonColumn" to "",
      "addzero.modbus.apiClientPackageName" to "",
      "addzero.modbus.apiClientOutputDir" to "",
      "addzero.modbus.spring.route.outputDir" to "",
      "addzero.modbus.address.lock.path" to "",
      "addzero.modbus.rtu.default.portPath" to "/dev/ttyUSB0",
      "addzero.modbus.rtu.default.unitId" to "1",
      "addzero.modbus.rtu.default.baudRate" to "9600",
      "addzero.modbus.rtu.default.dataBits" to "8",
      "addzero.modbus.rtu.default.stopBits" to "1",
      "addzero.modbus.rtu.default.parity" to "none",
      "addzero.modbus.rtu.default.timeoutMs" to "1000",
      "addzero.modbus.rtu.default.retries" to "2",
      "addzero.modbus.tcp.default.host" to "127.0.0.1",
      "addzero.modbus.tcp.default.port" to "502",
      "addzero.modbus.tcp.default.unitId" to "1",
      "addzero.modbus.tcp.default.timeoutMs" to "1000",
      "addzero.modbus.tcp.default.retries" to "2",
      "addzero.modbus.mqtt.default.brokerUrl" to "tcp://127.0.0.1:1883",
      "addzero.modbus.mqtt.default.clientId" to "modbus-mqtt-client",
      "addzero.modbus.mqtt.default.requestTopic" to "modbus/request",
      "addzero.modbus.mqtt.default.responseTopic" to "modbus/response",
      "addzero.modbus.mqtt.default.qos" to "1",
      "addzero.modbus.mqtt.default.timeoutMs" to "1000",
      "addzero.modbus.mqtt.default.retries" to "2",
    ),
  )
}

dependencies {
  implementation(projects.lib.ksp.metadata.modbus.modbusCodegenCore)
  implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())

  testImplementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
  testImplementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
  testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

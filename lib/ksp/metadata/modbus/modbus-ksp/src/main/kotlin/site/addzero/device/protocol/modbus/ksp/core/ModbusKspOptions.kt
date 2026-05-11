package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import site.addzero.device.protocol.modbus.ksp.context.Settings

data class ModbusDatabaseMetadataOptions(
    val driverClass: String?,
    val jdbcUrl: String,
    val username: String?,
    val password: String?,
    val query: String,
    val jsonColumn: String?,
)

internal fun initializeModbusKspSettings(environment: SymbolProcessorEnvironment) {
    Settings.fromOptions(environment.options)
}

fun SymbolProcessorEnvironment.resolveContractPackages(): List<String> =
    (Settings.addzeroModbusContractPackages +
        Settings.addzeroModbusContractPackage
            .takeIf(String::isNotBlank)
            ?.let(::listOf)
            .orEmpty())
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()

fun SymbolProcessorEnvironment.resolveMetadataProviderIds(): List<String> =
    Settings.addzeroModbusMetadataProviders
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()

fun SymbolProcessorEnvironment.resolveDatabaseMetadataOptions(): ModbusDatabaseMetadataOptions? {
    val jdbcUrl = Settings.addzeroModbusDatabaseJdbcUrl.trim().takeIf(String::isNotBlank)
    val query = Settings.addzeroModbusDatabaseQuery.trim().takeIf(String::isNotBlank)
    if (jdbcUrl == null && query == null) {
        return null
    }
    require(jdbcUrl != null) {
        "Missing addzero.modbus.database.jdbcUrl for Modbus database metadata provider."
    }
    require(query != null) {
        "Missing addzero.modbus.database.query for Modbus database metadata provider."
    }
    return ModbusDatabaseMetadataOptions(
        driverClass = Settings.addzeroModbusDatabaseDriverClass.trim().takeIf(String::isNotBlank),
        jdbcUrl = jdbcUrl,
        username = Settings.addzeroModbusDatabaseUsername.trim().takeIf(String::isNotBlank),
        password = Settings.addzeroModbusDatabasePassword.trim().takeIf(String::isNotBlank),
        query = query,
        jsonColumn = Settings.addzeroModbusDatabaseJsonColumn.trim().takeIf(String::isNotBlank),
    )
}

fun SymbolProcessorEnvironment.resolveApiClientPackageName(): String? =
    Settings.addzeroModbusApiClientPackageName.trim().takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveApiClientOutputDir(): String? =
    Settings.addzeroModbusApiClientOutputDir.trim().takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveSpringRouteOutputDir(): String? =
    Settings.addzeroModbusSpringRouteOutputDir.trim().takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveAddressLockPath(): String? =
    Settings.addzeroModbusAddressLockPath.trim().takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveEnabledTransports(defaultTransport: ModbusTransportKind): Set<ModbusTransportKind> =
    ModbusTransportKind.resolveConfiguredOrDefault(
        rawValue = Settings.addzeroModbusTransports.joinToString(","),
        defaultTransport = defaultTransport,
    )

fun SymbolProcessorEnvironment.resolveTransportDefaults(): ModbusTransportDefaults =
    ModbusTransportDefaults(
        rtu =
            ModbusRtuTransportDefaults(
                portPath = Settings.addzeroModbusRtuDefaultPortPath.ifBlank { "/dev/ttyUSB0" },
                unitId = Settings.addzeroModbusRtuDefaultUnitId,
                baudRate = Settings.addzeroModbusRtuDefaultBaudRate,
                dataBits = Settings.addzeroModbusRtuDefaultDataBits,
                stopBits = Settings.addzeroModbusRtuDefaultStopBits,
                parity = Settings.addzeroModbusRtuDefaultParity.ifBlank { "none" },
                timeoutMs = Settings.addzeroModbusRtuDefaultTimeoutMs.toLong(),
                retries = Settings.addzeroModbusRtuDefaultRetries,
            ),
        tcp =
            ModbusTcpTransportDefaults(
                host = Settings.addzeroModbusTcpDefaultHost.ifBlank { "127.0.0.1" },
                port = Settings.addzeroModbusTcpDefaultPort,
                unitId = Settings.addzeroModbusTcpDefaultUnitId,
                timeoutMs = Settings.addzeroModbusTcpDefaultTimeoutMs.toLong(),
                retries = Settings.addzeroModbusTcpDefaultRetries,
            ),
        mqtt =
            ModbusMqttTransportDefaults(
                brokerUrl = Settings.addzeroModbusMqttDefaultBrokerUrl.ifBlank { "tcp://127.0.0.1:1883" },
                clientId = Settings.addzeroModbusMqttDefaultClientId.ifBlank { "modbus-mqtt-client" },
                requestTopic = Settings.addzeroModbusMqttDefaultRequestTopic.ifBlank { "modbus/request" },
                responseTopic = Settings.addzeroModbusMqttDefaultResponseTopic.ifBlank { "modbus/response" },
                qos = Settings.addzeroModbusMqttDefaultQos,
                timeoutMs = Settings.addzeroModbusMqttDefaultTimeoutMs.toLong(),
                retries = Settings.addzeroModbusMqttDefaultRetries,
            ),
    )

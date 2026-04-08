package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

object ModbusKspOptions {
    const val TRANSPORTS_OPTION = "addzero.modbus.transports"
    const val CONTRACT_PACKAGES_OPTION = "addzero.modbus.contractPackages"
    const val CONTRACT_PACKAGE_OPTION = "addzero.modbus.contractPackage"
    const val METADATA_PROVIDERS_OPTION = "addzero.modbus.metadata.providers"
    const val DATABASE_DRIVER_CLASS_OPTION = "addzero.modbus.database.driverClass"
    const val DATABASE_JDBC_URL_OPTION = "addzero.modbus.database.jdbcUrl"
    const val DATABASE_USERNAME_OPTION = "addzero.modbus.database.username"
    const val DATABASE_PASSWORD_OPTION = "addzero.modbus.database.password"
    const val DATABASE_QUERY_OPTION = "addzero.modbus.database.query"
    const val DATABASE_JSON_COLUMN_OPTION = "addzero.modbus.database.jsonColumn"
    const val API_CLIENT_PACKAGE_OPTION = "addzero.modbus.apiClientPackageName"
    const val API_CLIENT_OUTPUT_DIR_OPTION = "addzero.modbus.apiClientOutputDir"
    const val SPRING_ROUTE_OUTPUT_DIR_OPTION = "addzero.modbus.spring.route.outputDir"
    const val RTU_PORT_PATH_OPTION = "addzero.modbus.rtu.default.portPath"
    const val RTU_UNIT_ID_OPTION = "addzero.modbus.rtu.default.unitId"
    const val RTU_BAUD_RATE_OPTION = "addzero.modbus.rtu.default.baudRate"
    const val RTU_DATA_BITS_OPTION = "addzero.modbus.rtu.default.dataBits"
    const val RTU_STOP_BITS_OPTION = "addzero.modbus.rtu.default.stopBits"
    const val RTU_PARITY_OPTION = "addzero.modbus.rtu.default.parity"
    const val RTU_TIMEOUT_MS_OPTION = "addzero.modbus.rtu.default.timeoutMs"
    const val RTU_RETRIES_OPTION = "addzero.modbus.rtu.default.retries"
    const val TCP_HOST_OPTION = "addzero.modbus.tcp.default.host"
    const val TCP_PORT_OPTION = "addzero.modbus.tcp.default.port"
    const val TCP_UNIT_ID_OPTION = "addzero.modbus.tcp.default.unitId"
    const val TCP_TIMEOUT_MS_OPTION = "addzero.modbus.tcp.default.timeoutMs"
    const val TCP_RETRIES_OPTION = "addzero.modbus.tcp.default.retries"
}

data class ModbusDatabaseMetadataOptions(
    val driverClass: String?,
    val jdbcUrl: String,
    val username: String?,
    val password: String?,
    val query: String,
    val jsonColumn: String?,
)

fun SymbolProcessorEnvironment.resolveContractPackages(): List<String> =
    listOfNotNull(
        options[ModbusKspOptions.CONTRACT_PACKAGES_OPTION],
        options[ModbusKspOptions.CONTRACT_PACKAGE_OPTION],
    ).flatMap { rawValue ->
        rawValue
            .split(',', ';', '\n')
            .map(String::trim)
            .filter(String::isNotBlank)
    }.distinct()

fun SymbolProcessorEnvironment.resolveMetadataProviderIds(): List<String> =
    options[ModbusKspOptions.METADATA_PROVIDERS_OPTION]
        ?.split(',', ';', '\n')
        ?.map(String::trim)
        ?.filter(String::isNotBlank)
        .orEmpty()
        .distinct()

fun SymbolProcessorEnvironment.resolveDatabaseMetadataOptions(): ModbusDatabaseMetadataOptions? {
    val jdbcUrl =
        options[ModbusKspOptions.DATABASE_JDBC_URL_OPTION]
            ?.trim()
            ?.takeIf(String::isNotBlank)
    val query =
        options[ModbusKspOptions.DATABASE_QUERY_OPTION]
            ?.trim()
            ?.takeIf(String::isNotBlank)
    if (jdbcUrl == null && query == null) {
        return null
    }
    require(jdbcUrl != null) {
        "Missing ${ModbusKspOptions.DATABASE_JDBC_URL_OPTION} for Modbus database metadata provider."
    }
    require(query != null) {
        "Missing ${ModbusKspOptions.DATABASE_QUERY_OPTION} for Modbus database metadata provider."
    }
    return ModbusDatabaseMetadataOptions(
        driverClass =
            options[ModbusKspOptions.DATABASE_DRIVER_CLASS_OPTION]
                ?.trim()
                ?.takeIf(String::isNotBlank),
        jdbcUrl = jdbcUrl,
        username =
            options[ModbusKspOptions.DATABASE_USERNAME_OPTION]
                ?.trim()
                ?.takeIf(String::isNotBlank),
        password =
            options[ModbusKspOptions.DATABASE_PASSWORD_OPTION]
                ?.trim()
                ?.takeIf(String::isNotBlank),
        query = query,
        jsonColumn =
            options[ModbusKspOptions.DATABASE_JSON_COLUMN_OPTION]
                ?.trim()
                ?.takeIf(String::isNotBlank),
    )
}

fun SymbolProcessorEnvironment.resolveApiClientPackageName(): String? =
    options[ModbusKspOptions.API_CLIENT_PACKAGE_OPTION]
        ?.trim()
        ?.takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveApiClientOutputDir(): String? =
    options[ModbusKspOptions.API_CLIENT_OUTPUT_DIR_OPTION]
        ?.trim()
        ?.takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveSpringRouteOutputDir(): String? =
    options[ModbusKspOptions.SPRING_ROUTE_OUTPUT_DIR_OPTION]
        ?.trim()
        ?.takeIf(String::isNotBlank)

fun SymbolProcessorEnvironment.resolveEnabledTransports(defaultTransport: ModbusTransportKind): Set<ModbusTransportKind> =
    ModbusTransportKind.resolveConfiguredOrDefault(
        rawValue = options[ModbusKspOptions.TRANSPORTS_OPTION],
        defaultTransport = defaultTransport,
    )

fun SymbolProcessorEnvironment.resolveTransportDefaults(): ModbusTransportDefaults =
    ModbusTransportDefaults(
        rtu =
            ModbusRtuTransportDefaults(
                portPath = options[ModbusKspOptions.RTU_PORT_PATH_OPTION].orEmpty().ifBlank { "/dev/ttyUSB0" },
                unitId = options[ModbusKspOptions.RTU_UNIT_ID_OPTION].toIntOrDefault(1),
                baudRate = options[ModbusKspOptions.RTU_BAUD_RATE_OPTION].toIntOrDefault(9600),
                dataBits = options[ModbusKspOptions.RTU_DATA_BITS_OPTION].toIntOrDefault(8),
                stopBits = options[ModbusKspOptions.RTU_STOP_BITS_OPTION].toIntOrDefault(1),
                parity = options[ModbusKspOptions.RTU_PARITY_OPTION].orEmpty().ifBlank { "none" },
                timeoutMs = options[ModbusKspOptions.RTU_TIMEOUT_MS_OPTION].toLongOrDefault(1_000),
                retries = options[ModbusKspOptions.RTU_RETRIES_OPTION].toIntOrDefault(2),
            ),
        tcp =
            ModbusTcpTransportDefaults(
                host = options[ModbusKspOptions.TCP_HOST_OPTION].orEmpty().ifBlank { "127.0.0.1" },
                port = options[ModbusKspOptions.TCP_PORT_OPTION].toIntOrDefault(502),
                unitId = options[ModbusKspOptions.TCP_UNIT_ID_OPTION].toIntOrDefault(1),
                timeoutMs = options[ModbusKspOptions.TCP_TIMEOUT_MS_OPTION].toLongOrDefault(1_000),
                retries = options[ModbusKspOptions.TCP_RETRIES_OPTION].toIntOrDefault(2),
            ),
    )

private fun String?.toIntOrDefault(defaultValue: Int): Int =
    this
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.toIntOrNull()
        ?: defaultValue

private fun String?.toLongOrDefault(defaultValue: Long): Long =
    this
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.toLongOrNull()
        ?: defaultValue

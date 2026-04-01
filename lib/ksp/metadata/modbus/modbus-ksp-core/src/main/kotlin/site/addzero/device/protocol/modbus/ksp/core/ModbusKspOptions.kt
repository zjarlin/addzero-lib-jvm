package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

object ModbusKspOptions {
    const val TRANSPORTS_OPTION: String = "addzero.modbus.transports"
    const val CONTRACT_PACKAGES_OPTION: String = "addzero.modbus.contractPackages"
    const val CONTRACT_PACKAGE_OPTION: String = "addzero.modbus.contractPackage"
}

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

fun SymbolProcessorEnvironment.resolveEnabledTransports(defaultTransport: ModbusTransportKind): Set<ModbusTransportKind> =
    ModbusTransportKind.resolveConfiguredOrDefault(
        rawValue = options[ModbusKspOptions.TRANSPORTS_OPTION],
        defaultTransport = defaultTransport,
    )

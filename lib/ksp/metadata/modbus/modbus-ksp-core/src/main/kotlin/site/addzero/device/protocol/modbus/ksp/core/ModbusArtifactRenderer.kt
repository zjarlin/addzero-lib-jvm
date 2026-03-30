package site.addzero.device.protocol.modbus.ksp.core

/**
 * 统一渲染 Kotlin 与 C 产物。
 */
object ModbusArtifactRenderer {
    fun renderServerArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> {
        if (services.isEmpty()) {
            return emptyList()
        }

        val fileContent =
            buildString {
                appendLine("package ${transport.generatedPackage}")
                appendLine()
                appendLine("import io.ktor.server.request.receive")
                appendLine("import io.ktor.server.response.respond")
                appendLine("import io.ktor.server.routing.Route")
                appendLine("import io.ktor.server.routing.post")
                appendLine("import kotlinx.serialization.Serializable")
                appendLine("import org.koin.core.annotation.Module")
                appendLine("import org.koin.core.annotation.Single")
                appendLine("import org.koin.mp.KoinPlatform")
                when (transport) {
                    ModbusTransportKind.RTU -> {
                        appendLine("import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigProvider")
                        appendLine("import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry")
                        appendLine("import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig")
                        appendLine("import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor")
                        appendLine("import site.addzero.device.driver.modbus.rtu.ModbusSerialParity")
                    }

                    ModbusTransportKind.TCP -> {
                        appendLine("import site.addzero.device.driver.modbus.tcp.ModbusTcpConfigProvider")
                        appendLine("import site.addzero.device.driver.modbus.tcp.ModbusTcpConfigRegistry")
                        appendLine("import site.addzero.device.driver.modbus.tcp.ModbusTcpEndpointConfig")
                        appendLine("import site.addzero.device.driver.modbus.tcp.ModbusTcpExecutor")
                    }
                }
                appendLine("import site.addzero.device.protocol.modbus.ModbusCodecSupport")
                appendLine("import site.addzero.device.protocol.modbus.model.ModbusCodec")
                appendLine()
                append(renderTransportRequestSupport(transport))
                appendLine()
                services.forEach { service ->
                    append(renderRequestClasses(service))
                    appendLine()
                    append(renderConfigProvider(service))
                    appendLine()
                    append(renderGateway(service))
                    appendLine()
                }
                append(renderModule(transport, services))
                appendLine()
                append(renderRouteRegistrar(transport, services))
            }

        return listOf(
            GeneratedArtifact(
                packageName = transport.generatedPackage,
                fileName = transport.generatedFileName,
                extensionName = "kt",
                content = fileContent,
            )
        )
    }

    fun renderContractArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> {
        val outputPackage = "generated.modbus.${service.transport.transportId}"
        val cServiceName = service.cServiceName
        val protocolFileBaseName = "${service.protocolDocBaseName()}.${service.transport.transportId}.protocol"
        return listOf(
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = "${cServiceName}_generated",
                extensionName = "h",
                content = renderGeneratedHeader(service),
            ),
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = "${cServiceName}_generated",
                extensionName = "c",
                content = renderGeneratedSource(service),
            ),
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = "${cServiceName}_user_contract",
                extensionName = "h",
                content = renderUserContractHeader(service),
            ),
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = "${cServiceName}_user_impl",
                extensionName = "sample.c",
                content = renderUserSampleSource(service),
            ),
            GeneratedArtifact(
                packageName = "generated.modbus.protocols",
                fileName = protocolFileBaseName,
                extensionName = "md",
                content = renderProtocolMarkdown(service),
            ),
        )
    }

    fun renderTransportContractArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> {
        if (services.isEmpty()) {
            return emptyList()
        }

        val outputPackage = "generated.modbus.${transport.transportId}"
        return listOf(
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = transport.dispatchFileName(),
                extensionName = "h",
                content = renderTransportDispatchHeader(transport, services),
            ),
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = transport.dispatchFileName(),
                extensionName = "c",
                content = renderTransportDispatchSource(transport, services),
            ),
        )
    }

    private fun renderProtocolMarkdown(service: ModbusServiceModel): String =
        buildString {
            appendLine("# ${service.interfaceSimpleName} Protocol Metadata (${service.transport.displayName})")
            appendLine()
            appendLine("这份文档由 Modbus contract KSP 自动生成。")
            appendLine()
            appendLine("## Service Overview")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("Key", "Value"),
                    rows =
                        listOf(
                            listOf("Service ID", "`${service.serviceId}`"),
                            listOf("Transport", "`${service.transport.displayName}`"),
                            listOf("Base Path", "`${service.basePath}`"),
                            listOf("Interface", "`${service.interfaceQualifiedName}`"),
                            listOf("Summary", service.summary.ifBlank { "-" }),
                        ),
                )
            )
            appendLine()
            appendLine("## Transport Defaults")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("Field", "Default"),
                    rows = renderTransportDefaultsMarkdownRows(service.transport),
                )
            )
            appendLine()
            appendLine("## Operations Summary")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("Operation ID", "Method", "Function Code", "Address", "Quantity", "Return", "Summary"),
                    rows =
                        service.operations.map { operation ->
                            listOf(
                                "`${operation.operationId}`",
                                "`${operation.methodName}`",
                                "`${operation.functionCodeName}`",
                                "`${operation.address}`",
                                "`${operation.quantity}`",
                                operation.returnType.renderProtocolReturnSummary(),
                                operation.doc.summary.ifBlank { "-" },
                            )
                        },
                )
            )
            service.operations.forEach { operation ->
                appendLine()
                appendLine("## `${operation.operationId}`")
                appendLine()
                append(
                    renderMarkdownTable(
                        headers = listOf("Field", "Value"),
                        rows =
                            buildList {
                                add(listOf("Method", "`${operation.methodName}`"))
                                add(listOf("Function Code", "`${operation.functionCodeName}`"))
                                add(listOf("Address", "`${operation.address}`"))
                                add(listOf("Quantity", "`${operation.quantity}`"))
                                add(listOf("Return Type", operation.returnType.renderProtocolReturnSummary()))
                                add(listOf("Summary", operation.doc.summary.ifBlank { "-" }))
                            },
                    )
                )
                if (operation.parameters.isNotEmpty()) {
                    appendLine()
                    appendLine("### Parameters")
                    appendLine()
                    append(
                        renderMarkdownTable(
                            headers = listOf("Name", "Type", "Codec", "Register Offset", "Bit Offset", "Width", "Description"),
                            rows =
                                operation.parameters.map { parameter ->
                                    listOf(
                                        "`${parameter.name}`",
                                        "`${parameter.kotlinTypeSimpleName()}`",
                                        "`${parameter.codecName}`",
                                        "`${parameter.registerOffset}`",
                                        "`${parameter.bitOffset}`",
                                        "`${parameter.registerWidth}`",
                                        parameter.doc.ifBlank { "-" },
                                    )
                                },
                        )
                    )
                }
                if (operation.returnType.kind == ModbusReturnKind.DTO) {
                    appendLine()
                    appendLine("### Return Fields")
                    appendLine()
                    append(
                        renderMarkdownTable(
                            headers = listOf("Name", "Type", "Codec", "Register Offset", "Bit Offset", "Width", "Description"),
                            rows =
                                operation.returnType.properties.map { property ->
                                    val field = property.field
                                    listOf(
                                        "`${property.name}`",
                                        "`${property.kotlinTypeSimpleName()}`",
                                        "`${field?.codecName ?: "n/a"}`",
                                        "`${field?.registerOffset?.toString() ?: "-"}`",
                                        "`${field?.bitOffset?.toString() ?: "-"}`",
                                        "`${field?.registerWidth?.toString() ?: "-"}`",
                                        property.doc.ifBlank { "-" },
                                    )
                                },
                        )
                    )
                }
            }
        }

    private fun renderTransportDispatchHeader(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String =
        buildString {
            val guard = "${transport.dispatchFileName()}_h".uppercase()
            appendLine("#ifndef $guard")
            appendLine("#define $guard")
            appendLine()
            appendLine("#include <stdbool.h>")
            appendLine("#include <stddef.h>")
            appendLine("#include <stdint.h>")
            appendLine()
            services.forEach { service ->
                appendLine("#include \"${service.cServiceName}_generated.h\"")
            }
            appendLine()
            appendLine("/*")
            appendLine(" * ${transport.displayName} 聚合分发入口。")
            appendLine(" * 生成层已经把 address / quantity / 编解码粘合逻辑收口到这里。")
            appendLine(" * 固件侧只需要把运行时收到的请求转发到这些函数，再实现各个 *_user_* 业务方法。")
            appendLine(" */")
            appendLine("typedef struct {")
            appendLine("    bool accepted;")
            appendLine("    const char *summary;")
            appendLine("} ${transport.dispatchResultTypeName()};")
            appendLine()
            appendLine("bool ${transport.dispatchFunctionPrefix()}_read_coils(uint16_t start_address, uint16_t quantity, bool *out_coils);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_read_discrete_inputs(uint16_t start_address, uint16_t quantity, bool *out_inputs);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_read_input_registers(uint16_t start_address, uint16_t quantity, uint16_t *out_registers);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_read_holding_registers(uint16_t start_address, uint16_t quantity, uint16_t *out_registers);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_write_single_coil(uint16_t address, bool value, ${transport.dispatchResultTypeName()} *out_result);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_write_multiple_coils(uint16_t start_address, uint16_t quantity, const bool *input_coils, ${transport.dispatchResultTypeName()} *out_result);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_write_single_register(uint16_t address, uint16_t value, ${transport.dispatchResultTypeName()} *out_result);")
            appendLine("bool ${transport.dispatchFunctionPrefix()}_write_multiple_registers(uint16_t start_address, uint16_t quantity, const uint16_t *input_registers, ${transport.dispatchResultTypeName()} *out_result);")
            appendLine()
            appendLine("#endif")
        }

    private fun renderTransportDispatchSource(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String =
        buildString {
            appendLine("#include \"${transport.dispatchFileName()}.h\"")
            appendLine()
            appendLine(
                "static void ${transport.dispatchFunctionPrefix()}_set_result(" +
                    "${transport.dispatchResultTypeName()} *out_result, " +
                    "bool accepted, " +
                    "const char *summary" +
                    ") {",
            )
            appendLine("    if (out_result == NULL) {")
            appendLine("        return;")
            appendLine("    }")
            appendLine("    out_result->accepted = accepted;")
            appendLine("    out_result->summary = summary;")
            appendLine("}")
            appendLine()
            append(renderTransportReadDispatch(transport, services, "READ_COILS"))
            appendLine()
            append(renderTransportReadDispatch(transport, services, "READ_DISCRETE_INPUTS"))
            appendLine()
            append(renderTransportReadDispatch(transport, services, "READ_INPUT_REGISTERS"))
            appendLine()
            append(renderTransportReadDispatch(transport, services, "READ_HOLDING_REGISTERS"))
            appendLine()
            append(renderTransportWriteSingleCoilDispatch(transport, services))
            appendLine()
            append(renderTransportWriteMultipleCoilsDispatch(transport, services))
            appendLine()
            append(renderTransportWriteSingleRegisterDispatch(transport, services))
            appendLine()
            append(renderTransportWriteMultipleRegistersDispatch(transport, services))
        }

    private fun renderTransportReadDispatch(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
        functionCodeName: String,
    ): String {
        val functionName = transport.readDispatchFunctionName(functionCodeName)
        val outputType =
            when (functionCodeName) {
                "READ_COILS" -> "bool *out_coils"
                "READ_DISCRETE_INPUTS" -> "bool *out_inputs"
                "READ_INPUT_REGISTERS",
                "READ_HOLDING_REGISTERS" -> "uint16_t *out_registers"
                else -> error("不支持的读功能码：$functionCodeName")
            }
        val outputName =
            when (functionCodeName) {
                "READ_COILS" -> "out_coils"
                "READ_DISCRETE_INPUTS" -> "out_inputs"
                "READ_INPUT_REGISTERS",
                "READ_HOLDING_REGISTERS" -> "out_registers"
                else -> error("不支持的读功能码：$functionCodeName")
            }
        val operations =
            services.flatMap { service ->
                service.operations
                    .filter { operation -> operation.functionCodeName == functionCodeName }
                    .map { operation -> service to operation }
            }
        return buildString {
            appendLine("bool $functionName(uint16_t start_address, uint16_t quantity, $outputType) {")
            appendLine("    if ($outputName == NULL) {")
                appendLine("        return false;")
                appendLine("    }")
            appendLine()
            appendLine("    switch (start_address) {")
            operations.forEach { (service, operation) ->
                appendLine("        case ${operation.macroPrefix(service)}_ADDRESS:")
                appendLine("            if (quantity != ${operation.macroPrefix(service)}_QUANTITY) {")
                appendLine("                return false;")
                appendLine("            }")
                appendLine("            return ${operation.dispatchFunctionName(service)}($outputName, quantity);")
            }
            appendLine("        default:")
            appendLine("            return false;")
            appendLine("    }")
            appendLine("}")
        }
    }

    private fun renderTransportWriteSingleCoilDispatch(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String {
        val operations =
            services.flatMap { service ->
                service.operations
                    .filter { operation -> operation.functionCodeName == "WRITE_SINGLE_COIL" }
                    .map { operation -> service to operation }
            }
        return buildString {
            appendLine(
                "bool ${transport.dispatchFunctionPrefix()}_write_single_coil(" +
                    "uint16_t address, " +
                    "bool value, " +
                    "${transport.dispatchResultTypeName()} *out_result" +
                    ") {",
            )
            appendLine("    if (out_result == NULL) {")
            appendLine("        return false;")
            appendLine("    }")
            appendLine()
            appendLine("    switch (address) {")
            operations.forEach { (service, operation) ->
                appendLine("        case ${operation.macroPrefix(service)}_ADDRESS: {")
                appendLine("            const bool input_coils[${operation.quantity}] = {value};")
                appendLine("            ${service.cServiceName}_command_result_t service_result = {0};")
                appendLine("            const bool handled = ${operation.dispatchFunctionName(service)}(input_coils, ${operation.quantity}, &service_result);")
                appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, service_result.accepted, service_result.summary);")
                appendLine("            return handled;")
                appendLine("        }")
            }
            appendLine("        default:")
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"未支持的写线圈地址。\");")
            appendLine("            return false;")
            appendLine("    }")
            appendLine("}")
        }
    }

    private fun renderTransportWriteMultipleCoilsDispatch(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String {
        val operations =
            services.flatMap { service ->
                service.operations
                    .filter { operation -> operation.functionCodeName == "WRITE_MULTIPLE_COILS" }
                    .map { operation -> service to operation }
            }
        return buildString {
            appendLine(
                "bool ${transport.dispatchFunctionPrefix()}_write_multiple_coils(" +
                    "uint16_t start_address, " +
                    "uint16_t quantity, " +
                    "const bool *input_coils, " +
                    "${transport.dispatchResultTypeName()} *out_result" +
                    ") {",
            )
            appendLine("    if (out_result == NULL || input_coils == NULL) {")
            appendLine("        return false;")
            appendLine("    }")
            appendLine()
            appendLine("    switch (start_address) {")
            operations.forEach { (service, operation) ->
                appendLine("        case ${operation.macroPrefix(service)}_ADDRESS: {")
                appendLine("            if (quantity != ${operation.macroPrefix(service)}_QUANTITY) {")
                appendLine("                ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"${escapeCComment(operation.operationId)} 线圈数量不匹配。\");")
                appendLine("                return false;")
                appendLine("            }")
                appendLine("            ${service.cServiceName}_command_result_t service_result = {0};")
                appendLine("            const bool handled = ${operation.dispatchFunctionName(service)}(input_coils, quantity, &service_result);")
                appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, service_result.accepted, service_result.summary);")
                appendLine("            return handled;")
                appendLine("        }")
            }
            appendLine("        default:")
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"未支持的多线圈写地址。\");")
            appendLine("            return false;")
            appendLine("    }")
            appendLine("}")
        }
    }

    private fun renderTransportWriteSingleRegisterDispatch(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String {
        val operations =
            services.flatMap { service ->
                service.operations
                    .filter { operation -> operation.functionCodeName == "WRITE_SINGLE_REGISTER" }
                    .map { operation -> service to operation }
            }
        return buildString {
            appendLine(
                "bool ${transport.dispatchFunctionPrefix()}_write_single_register(" +
                    "uint16_t address, " +
                    "uint16_t value, " +
                    "${transport.dispatchResultTypeName()} *out_result" +
                    ") {",
            )
            appendLine("    if (out_result == NULL) {")
            appendLine("        return false;")
            appendLine("    }")
            appendLine()
            appendLine("    switch (address) {")
            operations.forEach { (service, operation) ->
                appendLine("        case ${operation.macroPrefix(service)}_ADDRESS: {")
                appendLine("            const uint16_t input_registers[${operation.quantity}] = {value};")
                appendLine("            ${service.cServiceName}_command_result_t service_result = {0};")
                appendLine("            const bool handled = ${operation.dispatchFunctionName(service)}(input_registers, ${operation.quantity}, &service_result);")
                appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, service_result.accepted, service_result.summary);")
                appendLine("            return handled;")
                appendLine("        }")
            }
            appendLine("        default:")
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"未支持的写单寄存器地址。\");")
            appendLine("            return false;")
            appendLine("    }")
            appendLine("}")
        }
    }

    private fun renderTransportWriteMultipleRegistersDispatch(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String {
        val operations =
            services.flatMap { service ->
                service.operations
                    .filter { operation -> operation.functionCodeName == "WRITE_MULTIPLE_REGISTERS" }
                    .map { operation -> service to operation }
            }
        return buildString {
            appendLine(
                "bool ${transport.dispatchFunctionPrefix()}_write_multiple_registers(" +
                    "uint16_t start_address, " +
                    "uint16_t quantity, " +
                    "const uint16_t *input_registers, " +
                    "${transport.dispatchResultTypeName()} *out_result" +
                    ") {",
            )
            appendLine("    if (out_result == NULL || input_registers == NULL) {")
            appendLine("        return false;")
            appendLine("    }")
            appendLine()
            appendLine("    switch (start_address) {")
            operations.forEach { (service, operation) ->
                appendLine("        case ${operation.macroPrefix(service)}_ADDRESS: {")
                appendLine("            if (quantity != ${operation.macroPrefix(service)}_QUANTITY) {")
                appendLine("                ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"${escapeCComment(operation.operationId)} 寄存器数量不匹配。\");")
                appendLine("                return false;")
                appendLine("            }")
                appendLine("            ${service.cServiceName}_command_result_t service_result = {0};")
                appendLine("            const bool handled = ${operation.dispatchFunctionName(service)}(input_registers, quantity, &service_result);")
                appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, service_result.accepted, service_result.summary);")
                appendLine("            return handled;")
                appendLine("        }")
            }
            appendLine("        default:")
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"未支持的写寄存器地址。\");")
            appendLine("            return false;")
            appendLine("    }")
            appendLine("}")
        }
    }

    private fun renderRequestClasses(service: ModbusServiceModel): String =
        buildString {
            service.operations.forEach { operation ->
                appendLine("/**")
                appendLine(" * ${escapeComment(operation.doc.summary)}")
                appendLine(" */")
                appendLine("@Serializable")
                appendLine("data class ${operation.requestClassName}(")
                appendLine(service.transport.requestConfigFields())
                if (operation.parameters.isNotEmpty()) {
                    operation.parameters.forEachIndexed { index, parameter ->
                        appendLine("    /** ${escapeComment(parameter.doc)} */")
                        append("    val ${parameter.name}: ${parameter.kotlinTypeSimpleName()}")
                        appendLine(if (index == operation.parameters.lastIndex) "" else ",")
                    }
                }
                appendLine(") : ${service.transport.requestConfigInterfaceName()}")
                appendLine()
            }
        }

    private fun renderConfigProvider(service: ModbusServiceModel): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${escapeComment(service.summary)}")
            appendLine(" */")
            appendLine("class ${service.configProviderClassName} : ${service.transport.configProviderSimpleName()} {")
            appendLine("    override val serviceId: String = \"${service.serviceId}\"")
            appendLine()
            appendLine("    override fun defaultConfig(): ${service.transport.endpointConfigSimpleName()} =")
            appendLine(
                when (service.transport) {
                    ModbusTransportKind.RTU ->
                        "        ModbusRtuEndpointConfig(serviceId = serviceId, portPath = \"/dev/ttyUSB0\", unitId = 1, baudRate = 115200, dataBits = 8, stopBits = 1, parity = ModbusSerialParity.NONE, timeoutMs = 1_000, retries = 2)"

                    ModbusTransportKind.TCP ->
                        "        ModbusTcpEndpointConfig(serviceId = serviceId, host = \"127.0.0.1\", port = 502, unitId = 1, timeoutMs = 1_000, retries = 2)"
                }
            )
            appendLine("}")
        }

    private fun renderGateway(service: ModbusServiceModel): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${escapeComment(service.doc.summary)}")
            appendLine(" *")
            appendLine(" * 该网关由 KSP 自动生成，负责把高阶 Kotlin 接口翻译成 ${service.transport.displayName} 调用。")
            appendLine(" */")
            appendLine(
                "class ${service.gatewayClassName}(" +
                    "private val configRegistry: ${service.transport.configRegistrySimpleName()}, " +
                    "private val executor: ${service.transport.executorSimpleName()}" +
                    ") : ${service.interfaceQualifiedName} {"
            )
            appendLine("    fun defaultConfig(): ${service.transport.endpointConfigSimpleName()} = configRegistry.require(\"${service.serviceId}\")")
            appendLine()
            appendLine("    private fun resolveConfig(config: ${service.transport.endpointConfigSimpleName()}?): ${service.transport.endpointConfigSimpleName()} =")
            appendLine("        config ?: defaultConfig()")
            appendLine()
            service.operations.forEach { operation ->
                appendLine(
                    "    override suspend fun ${operation.methodName}(" +
                        operation.parameters.joinToString(", ") { parameter ->
                            "${parameter.name}: ${parameter.kotlinTypeSimpleName()}"
                        } +
                        "): ${operation.returnType.renderKotlinType()} = ${operation.methodName}(config = null${operation.renderGatewayCallArguments(prefix = ", ")})"
                )
                appendLine()
                appendLine(
                    "    suspend fun ${operation.methodName}(" +
                        "config: ${service.transport.endpointConfigSimpleName()}? = null" +
                        operation.renderGatewaySignatureSuffix() +
                        "): ${operation.returnType.renderKotlinType()} {"
                )
                appendLine("        val resolvedConfig = resolveConfig(config)")
                renderGatewayExecution(operation).forEach { line -> appendLine("        $line") }
                when (operation.returnType.kind) {
                    ModbusReturnKind.UNIT -> appendLine("        return Unit")
                    ModbusReturnKind.COMMAND_RESULT ->
                        appendLine(
                            "        return ${operation.returnType.qualifiedName}(accepted = true, summary = \"操作已下发：${operation.operationId}\")"
                        )

                    ModbusReturnKind.BOOLEAN ->
                        appendLine("        return ${operation.renderBooleanDecodeExpression()}")

                    ModbusReturnKind.INT ->
                        appendLine("        return ModbusCodecSupport.decodeInt(ModbusCodec.U16, registers, 0)")

                    ModbusReturnKind.DTO -> {
                        appendLine("        return ${operation.returnType.qualifiedName}(")
                        operation.returnType.properties.forEachIndexed { index, property ->
                            append("            ${property.name} = ${property.renderDecodeExpression()}")
                            appendLine(if (index == operation.returnType.properties.lastIndex) "" else ",")
                        }
                        appendLine("        )")
                    }
                }
                appendLine("    }")
                appendLine()
            }
            appendLine("}")
        }

    private fun renderModule(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${transport.displayName} 自动生成的 Koin 模块。")
            appendLine(" *")
            appendLine(" * 统一收口生成出来的默认配置提供器、配置注册表与网关。")
            appendLine(" */")
            appendLine("@Module")
            appendLine("class ${transport.generatedKoinModuleClassName()} {")
            services.forEach { service ->
                appendLine("    @Single")
                appendLine(
                    "    fun ${service.configProviderClassName.asProviderMethodName()}(): ${service.configProviderClassName} = " +
                        "${service.configProviderClassName}()",
                )
                appendLine()
            }
            appendLine("    @Single")
            appendLine("    fun ${transport.configRegistrySimpleName().asProviderMethodName()}(")
            services.forEachIndexed { index, service ->
                append("        ${service.configProviderClassName.asConstructorParameterName()}: ${service.configProviderClassName}")
                appendLine(if (index == services.lastIndex) "" else ",")
            }
            appendLine("    ): ${transport.configRegistrySimpleName()} =")
            appendLine(
                "        ${transport.configRegistrySimpleName()}(" +
                    "listOf(" +
                    services.joinToString(", ") { service -> service.configProviderClassName.asConstructorParameterName() } +
                    ")" +
                    ")",
            )
            appendLine()
            services.forEach { service ->
                appendLine("    @Single")
                appendLine("    fun ${service.gatewayClassName.asProviderMethodName()}(")
                appendLine("        configRegistry: ${transport.configRegistrySimpleName()},")
                appendLine("        executor: ${transport.executorSimpleName()},")
                appendLine("    ): ${service.gatewayClassName} = ${service.gatewayClassName}(configRegistry, executor)")
                appendLine()
            }
            appendLine("}")
        }

    private fun renderRouteRegistrar(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String =
        buildString {
            appendLine("fun Route.registerGeneratedModbus${transport.transportId.replaceFirstChar(Char::uppercase)}Routes() {")
            services.forEach { service ->
                service.operations.forEach { operation ->
                    val routePath = "${service.basePath}/${transport.transportId}/${service.serviceId}/${operation.operationId}"
                    appendLine("    post(\"$routePath\") {")
                    appendLine("        val request = call.receive<${operation.requestClassName}>()")
                    appendLine("        val gateway = KoinPlatform.getKoin().get<${service.gatewayClassName}>()")
                    appendLine("        val config = request.toEndpointConfig(gateway.defaultConfig())")
                    appendLine("        call.respond(gateway.${operation.methodName}(config = config${operation.renderGatewayArguments(valuePrefix = "request.")}))")
                    appendLine("    }")
                }
            }
            appendLine("}")
        }

    private fun renderGeneratedHeader(service: ModbusServiceModel): String =
        buildString {
            val guard = "${service.cServiceName}_generated_h".uppercase()
            appendLine("#ifndef $guard")
            appendLine("#define $guard")
            appendLine()
            appendLine("#include <stdbool.h>")
            appendLine("#include <stddef.h>")
            appendLine("#include <stdint.h>")
            appendLine()
            appendLine("/*")
            appendLine(" * ${escapeCComment(service.doc.summary)}")
            service.doc.descriptionLines.forEach { line ->
                appendLine(" * ${escapeCComment(line)}")
            }
            appendLine(" *")
            appendLine(" * 该文件由 ${service.transport.displayName} KSP 处理器自动生成，请勿直接手改。")
            appendLine(" */")
            appendLine()
            appendLine("#define ${service.cServiceName.uppercase()}_SERVICE_ID \"${service.serviceId}\"")
            service.operations.forEach { operation ->
                val operationMacro = "${service.cServiceName}_${operation.operationId}".uppercase().replace('-', '_')
                appendLine("#define ${operationMacro}_ADDRESS ${operation.address}")
                appendLine("#define ${operationMacro}_QUANTITY ${operation.quantity}")
            }
            appendLine()
            appendLine("typedef struct {")
            appendLine("    /* 命令是否被业务层受理。 */")
            appendLine("    bool accepted;")
            appendLine("    /* 业务层回传的中文说明。 */")
            appendLine("    const char *summary;")
            appendLine("} ${service.cServiceName}_command_result_t;")
            appendLine()
            service.operations.forEach { operation ->
                when (operation.returnType.kind) {
                    ModbusReturnKind.DTO -> {
                        appendLine("typedef struct {")
                        operation.returnType.properties.forEach { property ->
                            appendLine("    /* ${escapeCComment(property.doc)} */")
                            appendLine("    ${property.cType()} ${property.name.toSnakeCase()};")
                        }
                        appendLine("} ${service.cServiceName}_${operation.operationId.toSnakeCase()}_response_t;")
                        appendLine()
                    }

                    else -> Unit
                }
                if (operation.parameters.isNotEmpty()) {
                    appendLine("typedef struct {")
                    operation.parameters.forEach { parameter ->
                        appendLine("    /* ${escapeCComment(parameter.doc)} */")
                        appendLine("    ${parameter.cType()} ${parameter.name.toSnakeCase()};")
                    }
                    appendLine("} ${service.cServiceName}_${operation.operationId.toSnakeCase()}_request_t;")
                    appendLine()
                }
            }
            service.operations.forEach { operation ->
                appendLine("/* ${escapeCComment(operation.doc.summary)} */")
                appendLine("${operation.generatedDispatchSignature(service)};")
                appendLine()
            }
            appendLine("#endif")
        }

    private fun renderUserContractHeader(service: ModbusServiceModel): String =
        buildString {
            val guard = "${service.cServiceName}_user_contract_h".uppercase()
            appendLine("#ifndef $guard")
            appendLine("#define $guard")
            appendLine()
            appendLine("#include \"${service.cServiceName}_generated.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * 这个头文件属于固件同事的业务层扩展面。")
            appendLine(" * 生成层只会调用这里声明的函数，不会覆盖你手写的实现。")
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                appendLine("/* ${escapeCComment(operation.doc.summary)} */")
                appendLine("${operation.userContractSignature(service)};")
                appendLine()
            }
            appendLine("#endif")
        }

    private fun renderGeneratedSource(service: ModbusServiceModel): String =
        buildString {
            appendLine("#include \"${service.cServiceName}_generated.h\"")
            appendLine("#include \"${service.cServiceName}_user_contract.h\"")
            appendLine()
            service.operations.forEach { operation ->
                appendLine("${operation.generatedDispatchSignature(service)} {")
                operation.renderCDispatchBody(service).forEach { line -> appendLine("    $line") }
                appendLine("}")
                appendLine()
            }
        }

    private fun renderUserSampleSource(service: ModbusServiceModel): String =
        buildString {
            appendLine("#include \"${service.cServiceName}_user_contract.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * 示例实现文件。")
            appendLine(" * 请复制为你自己的 .c 文件后继续完善，不要直接修改这个 sample 文件。")
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                appendLine("${operation.userContractSignature(service)} {")
                appendLine("    /* ${escapeCComment(operation.doc.summary)} */")
                when (operation.returnType.kind) {
                    ModbusReturnKind.DTO -> {
                        appendLine("    if (out_response == NULL) {")
                        appendLine("        return false;")
                        appendLine("    }")
                        operation.returnType.properties.forEach { property ->
                            appendLine("    out_response->${property.name.toSnakeCase()} = 0;")
                        }
                        appendLine("    return true;")
                    }

                    ModbusReturnKind.COMMAND_RESULT -> {
                        appendLine("    if (out_result == NULL) {")
                        appendLine("        return false;")
                        appendLine("    }")
                        appendLine("    out_result->accepted = true;")
                        appendLine("    out_result->summary = \"请补充业务实现\";")
                        appendLine("    return true;")
                    }

                    ModbusReturnKind.BOOLEAN,
                    ModbusReturnKind.INT -> {
                        appendLine("    if (out_value == NULL) {")
                        appendLine("        return false;")
                        appendLine("    }")
                        appendLine("    *out_value = 0;")
                        appendLine("    return true;")
                    }

                    else -> {
                        appendLine("    /* TODO: 在这里接入真实硬件逻辑。 */")
                        appendLine("    return true;")
                    }
                }
                appendLine("}")
                appendLine()
            }
        }

    private fun renderGatewayExecution(operation: ModbusOperationModel): List<String> =
        when (operation.functionCodeName) {
            "READ_COILS" -> listOf("val registers = executor.readCoils(resolvedConfig, ${operation.address}, ${operation.quantity})")
            "READ_DISCRETE_INPUTS" ->
                listOf("val registers = executor.readDiscreteInputs(resolvedConfig, ${operation.address}, ${operation.quantity})")
            "READ_HOLDING_REGISTERS" -> listOf("val registers = executor.readHoldingRegisters(resolvedConfig, ${operation.address}, ${operation.quantity})")
            "READ_INPUT_REGISTERS" -> listOf("val registers = executor.readInputRegisters(resolvedConfig, ${operation.address}, ${operation.quantity})")
            "WRITE_SINGLE_COIL" -> {
                val parameter = operation.parameters.first()
                listOf("executor.writeSingleCoil(resolvedConfig, ${operation.address}, ${parameter.coilExpression()})")
            }

            "WRITE_MULTIPLE_COILS" ->
                buildList {
                    add("val coilValues = MutableList(${operation.quantity}) { false }")
                    operation.parameters.forEach { parameter ->
                        add("coilValues[${parameter.registerOffset}] = ${parameter.coilExpression()}")
                    }
                    add("executor.writeMultipleCoils(resolvedConfig, ${operation.address}, coilValues)")
                }

            "WRITE_SINGLE_REGISTER" -> {
                val parameter = operation.parameters.first()
                listOf(
                    "val registerValue = ${parameter.renderSingleRegisterEncodeExpression()}",
                    "executor.writeSingleRegister(resolvedConfig, ${operation.address}, registerValue)",
                )
            }

            "WRITE_MULTIPLE_REGISTERS" ->
                buildList {
                    add("val encodedValues = MutableList(${operation.quantity}) { 0 }")
                    operation.parameters.forEach { parameter ->
                        parameter.renderRegisterPackLines().forEach { line -> add(line) }
                    }
                    add("executor.writeMultipleRegisters(resolvedConfig, ${operation.address}, encodedValues)")
                }

            else -> listOf("error(\"暂不支持的功能码：${operation.functionCodeName}\")")
        }

    private fun ModbusOperationModel.renderGatewayArguments(valuePrefix: String = ""): String =
        if (parameters.isEmpty()) {
            ""
        } else {
            parameters.joinToString(prefix = ", ") { parameter -> "${parameter.name} = ${valuePrefix}${parameter.name}" }
        }

    private fun ModbusOperationModel.renderGatewayCallArguments(prefix: String = ""): String =
        if (parameters.isEmpty()) {
            ""
        } else {
            parameters.joinToString(prefix = prefix) { parameter -> "${parameter.name} = ${parameter.name}" }
        }

    private fun ModbusOperationModel.renderGatewaySignatureSuffix(): String =
        if (parameters.isEmpty()) {
            ""
        } else {
            parameters.joinToString(prefix = ", ") { parameter -> "${parameter.name}: ${parameter.kotlinTypeSimpleName()}" }
        }

    private fun ModbusOperationModel.generatedDispatchSignature(service: ModbusServiceModel): String =
        when (functionCodeName) {
            "READ_COILS",
            "READ_DISCRETE_INPUTS" ->
                "bool ${dispatchFunctionName(service)}(bool *out_coils, size_t coil_count)"

            "READ_HOLDING_REGISTERS",
            "READ_INPUT_REGISTERS" ->
                "bool ${dispatchFunctionName(service)}(uint16_t *out_registers, size_t register_count)"

            "WRITE_SINGLE_COIL",
            "WRITE_MULTIPLE_COILS" ->
                "bool ${dispatchFunctionName(service)}(const bool *input_coils, size_t coil_count, ${service.cServiceName}_command_result_t *out_result)"

            "WRITE_SINGLE_REGISTER",
            "WRITE_MULTIPLE_REGISTERS" ->
                "bool ${dispatchFunctionName(service)}(const uint16_t *input_registers, size_t register_count, ${service.cServiceName}_command_result_t *out_result)"

            else -> "bool ${dispatchFunctionName(service)}(void)"
        }

    private fun ModbusOperationModel.userContractSignature(service: ModbusServiceModel): String =
        when (returnType.kind) {
            ModbusReturnKind.DTO ->
                "bool ${userFunctionName(service)}(${responseStructName(service)} *out_response)"

            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT ->
                if (isReadOperation) {
                    "bool ${userFunctionName(service)}(${returnType.scalarOutType()} *out_value)"
                } else {
                    buildWriteUserContractSignature(service)
                }

            ModbusReturnKind.COMMAND_RESULT,
            ModbusReturnKind.UNIT -> buildWriteUserContractSignature(service)
        }

    private fun ModbusOperationModel.renderCDispatchBody(service: ModbusServiceModel): List<String> =
        if (isReadOperation) {
            renderReadDispatchBody(service)
        } else {
            renderWriteDispatchBody(service)
        }

    private fun ModbusOperationModel.buildWriteUserContractSignature(service: ModbusServiceModel): String {
        val requestPart =
            if (parameters.isEmpty()) {
                ""
            } else {
                "const ${requestStructName(service)} *request"
            }
        val resultPart =
            if (returnType.kind == ModbusReturnKind.COMMAND_RESULT) {
                "${if (requestPart.isNotEmpty()) ", " else ""}${service.cServiceName}_command_result_t *out_result"
            } else {
                ""
            }
        return "bool ${userFunctionName(service)}($requestPart$resultPart)"
    }

    private fun ModbusOperationModel.renderReadDispatchBody(service: ModbusServiceModel): List<String> =
        when (returnType.kind) {
            ModbusReturnKind.DTO ->
                if (usesCoilBits) {
                    buildList {
                        add("if (out_coils == NULL || coil_count < ${quantity}) {")
                        add("    return false;")
                        add("}")
                        add("${responseStructName(service)} response = {0};")
                        add("if (!${userFunctionName(service)}(&response)) {")
                        add("    return false;")
                        add("}")
                        returnType.properties.forEach { property ->
                            val field = property.field ?: return@forEach
                            add("out_coils[${field.registerOffset}] = response.${property.name.toSnakeCase()};")
                        }
                        add("return true;")
                    }
                } else {
                    buildList {
                        add("if (out_registers == NULL || register_count < ${quantity}) {")
                        add("    return false;")
                        add("}")
                        add("${responseStructName(service)} response = {0};")
                        add("if (!${userFunctionName(service)}(&response)) {")
                        add("    return false;")
                        add("}")
                        returnType.properties
                            .filter { property -> property.field != null }
                            .groupBy { property -> property.field!!.registerOffset }
                            .forEach { (registerOffset, properties) ->
                                add("out_registers[$registerOffset] = 0u;")
                                properties.forEach { property ->
                                    val field = property.field ?: return@forEach
                                    when (property.valueKind) {
                                        ModbusValueKind.BOOLEAN -> {
                                            if (field.codecName == "BIT_FLAG") {
                                                add("out_registers[$registerOffset] |= response.${property.name.toSnakeCase()} ? (uint16_t)(1u << ${field.bitOffset}) : 0u;")
                                            } else {
                                                add("out_registers[$registerOffset] = response.${property.name.toSnakeCase()} ? 1u : 0u;")
                                            }
                                        }

                                        ModbusValueKind.INT -> {
                                            if (field.codecName == "U32_BE") {
                                                add("out_registers[$registerOffset] = (uint16_t)((response.${property.name.toSnakeCase()} >> 16) & 0xFFFFu);")
                                                add("out_registers[${registerOffset + 1}] = (uint16_t)(response.${property.name.toSnakeCase()} & 0xFFFFu);")
                                            } else {
                                                add("out_registers[$registerOffset] = (uint16_t)(response.${property.name.toSnakeCase()});")
                                            }
                                        }
                                    }
                                }
                            }
                        add("return true;")
                    }
                }

            ModbusReturnKind.BOOLEAN ->
                buildList {
                    if (usesCoilBits) {
                        add("if (out_coils == NULL || coil_count < ${quantity}) {")
                    } else {
                        add("if (out_registers == NULL || register_count < ${quantity}) {")
                    }
                    add("    return false;")
                    add("}")
                    add("bool value = false;")
                    add("if (!${userFunctionName(service)}(&value)) {")
                    add("    return false;")
                    add("}")
                    if (usesCoilBits) {
                        add("out_coils[0] = value;")
                    } else {
                        add("out_registers[0] = value ? 1u : 0u;")
                    }
                    add("return true;")
                }

            ModbusReturnKind.INT ->
                buildList {
                    add("if (out_registers == NULL || register_count < ${quantity}) {")
                    add("    return false;")
                    add("}")
                    add("int32_t value = 0;")
                    add("if (!${userFunctionName(service)}(&value)) {")
                    add("    return false;")
                    add("}")
                    add("out_registers[0] = (uint16_t)(value);")
                    add("return true;")
                }

            ModbusReturnKind.UNIT,
            ModbusReturnKind.COMMAND_RESULT -> listOf("return false;")
        }

    private fun ModbusOperationModel.renderWriteDispatchBody(service: ModbusServiceModel): List<String> =
        buildList {
            val minimumRegisterCount = quantity.coerceAtLeast(1)
            if (usesCoilBits) {
                add("if (out_result == NULL || input_coils == NULL || coil_count < $minimumRegisterCount) {")
            } else {
                add("if (out_result == NULL || input_registers == NULL || register_count < $minimumRegisterCount) {")
            }
            add("    return false;")
            add("}")
            if (parameters.isNotEmpty()) {
                add("${requestStructName(service)} request = {0};")
                parameters.forEach { parameter ->
                    when {
                        usesCoilBits -> {
                            add("request.${parameter.name.toSnakeCase()} = input_coils[${parameter.registerOffset}];")
                        }

                        parameter.valueKind == ModbusValueKind.BOOLEAN && parameter.codecName == "BIT_FLAG" -> {
                            add("request.${parameter.name.toSnakeCase()} = (input_registers[${parameter.registerOffset}] & (uint16_t)(1u << ${parameter.bitOffset})) != 0u;")
                        }

                        parameter.valueKind == ModbusValueKind.INT && parameter.codecName == "U32_BE" -> {
                            add("request.${parameter.name.toSnakeCase()} = (int)(((uint32_t)input_registers[${parameter.registerOffset}] << 16) | input_registers[${parameter.registerOffset + 1}]);")
                        }

                        else -> {
                            add("request.${parameter.name.toSnakeCase()} = (int32_t)input_registers[${parameter.registerOffset}];")
                        }
                    }
                }
            }
            if (returnType.kind == ModbusReturnKind.COMMAND_RESULT) {
                add("return ${userFunctionName(service)}(${if (parameters.isNotEmpty()) "&request, " else ""}out_result);")
            } else if (parameters.isNotEmpty()) {
                add("out_result->accepted = ${userFunctionName(service)}(&request);")
                add("out_result->summary = out_result->accepted ? \"操作执行成功\" : \"操作执行失败\";")
                add("return out_result->accepted;")
            } else {
                add("out_result->accepted = ${userFunctionName(service)}();")
                add("out_result->summary = out_result->accepted ? \"操作执行成功\" : \"操作执行失败\";")
                add("return out_result->accepted;")
            }
        }

    private fun ModbusOperationModel.dispatchFunctionName(service: ModbusServiceModel): String =
        "${service.cServiceName}_handle_${operationId.toSnakeCase()}"

    private fun ModbusOperationModel.userFunctionName(service: ModbusServiceModel): String =
        "${service.cServiceName}_user_${operationId.toSnakeCase()}"

    private fun ModbusOperationModel.requestStructName(service: ModbusServiceModel): String =
        "${service.cServiceName}_${operationId.toSnakeCase()}_request_t"

    private fun ModbusOperationModel.responseStructName(service: ModbusServiceModel): String =
        "${service.cServiceName}_${operationId.toSnakeCase()}_response_t"

    private fun ModbusOperationModel.macroPrefix(service: ModbusServiceModel): String =
        "${service.cServiceName}_${operationId}".uppercase().replace('-', '_')

    private fun ModbusParameterModel.cType(): String =
        when (valueKind) {
            ModbusValueKind.BOOLEAN -> "bool"
            ModbusValueKind.INT -> "int32_t"
        }

    private fun ModbusPropertyModel.cType(): String =
        when (valueKind) {
            ModbusValueKind.BOOLEAN -> "bool"
            ModbusValueKind.INT -> "int32_t"
        }

    private fun ModbusReturnTypeModel.renderKotlinType(): String =
        when (kind) {
            ModbusReturnKind.UNIT -> "kotlin.Unit"
            else -> qualifiedName
        }

    private fun ModbusReturnTypeModel.scalarOutType(): String =
        when (kind) {
            ModbusReturnKind.BOOLEAN -> "bool"
            ModbusReturnKind.INT -> "int32_t"
            else -> error("只有标量返回才能生成 C out 参数类型：$kind")
        }

    private fun ModbusOperationModel.renderBooleanDecodeExpression(): String =
        when (functionCodeName) {
            "READ_COILS",
            "READ_DISCRETE_INPUTS" -> "registers.getOrElse(0) { 0 } != 0"
            "READ_INPUT_REGISTERS",
            "READ_HOLDING_REGISTERS" -> "registers.getOrElse(0) { 0 } != 0"
            else -> error("写功能码不支持布尔返回：$functionCodeName")
        }

    private fun ModbusPropertyModel.renderDecodeExpression(): String {
        val fieldModel = field ?: error("字段缺少 ModbusField 映射：$name")
        return when (valueKind) {
            ModbusValueKind.BOOLEAN ->
                "ModbusCodecSupport.decodeBoolean(ModbusCodec.${fieldModel.codecName}, registers, ${fieldModel.registerOffset}, ${fieldModel.bitOffset})"

            ModbusValueKind.INT ->
                "ModbusCodecSupport.decodeInt(ModbusCodec.${fieldModel.codecName}, registers, ${fieldModel.registerOffset})"
        }
    }

    private fun ModbusParameterModel.kotlinTypeSimpleName(): String =
        when (qualifiedType) {
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Int" -> "Int"
            else -> qualifiedType
        }

    private fun ModbusParameterModel.coilExpression(): String =
        if (valueKind == ModbusValueKind.BOOLEAN) {
            name
        } else {
            "ModbusCodecSupport.encodeValue(ModbusCodec.${codecName}, ${name}.toString()).first() != 0"
        }

    private fun ModbusParameterModel.renderSingleRegisterEncodeExpression(): String =
        when (codecName) {
            "BIT_FLAG" ->
                "if (${name}) (1 shl ${bitOffset}) else 0"

            else -> "ModbusCodecSupport.encodeValue(ModbusCodec.${codecName}, ${name}.toString()).first()"
        }

    private fun ModbusParameterModel.renderRegisterPackLines(): List<String> =
        when (codecName) {
            "BIT_FLAG" ->
                listOf(
                    "if (${name}) {",
                    "    encodedValues[${registerOffset}] = encodedValues[${registerOffset}] or (1 shl ${bitOffset})",
                    "}",
                )

            else ->
                listOf(
                    "ModbusCodecSupport.encodeValue(ModbusCodec.${codecName}, ${name}.toString())",
                    "    .forEachIndexed { index, value -> encodedValues[${registerOffset} + index] = value }",
                )
        }

    private fun ModbusPropertyModel.kotlinTypeSimpleName(): String =
        when (qualifiedType) {
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Int" -> "Int"
            else -> qualifiedType
        }

    private fun ModbusReturnTypeModel.renderProtocolReturnSummary(): String =
        when (kind) {
            ModbusReturnKind.UNIT -> "`Unit`"
            ModbusReturnKind.BOOLEAN -> "`Boolean`"
            ModbusReturnKind.INT -> "`Int`"
            ModbusReturnKind.COMMAND_RESULT -> "`ModbusCommandResult`"
            ModbusReturnKind.DTO -> "`${simpleName}`"
        }

    private fun renderTransportDefaultsMarkdownRows(transport: ModbusTransportKind): List<List<String>> =
        when (transport) {
            ModbusTransportKind.RTU ->
                listOf(
                    listOf("Port Path", "`/dev/ttyUSB0`"),
                    listOf("Unit ID", "`1`"),
                    listOf("Baud Rate", "`115200`"),
                    listOf("Data Bits", "`8`"),
                    listOf("Stop Bits", "`1`"),
                    listOf("Parity", "`none`"),
                    listOf("Timeout Ms", "`1000`"),
                    listOf("Retries", "`2`"),
                )

            ModbusTransportKind.TCP ->
                listOf(
                    listOf("Host", "`127.0.0.1`"),
                    listOf("Port", "`502`"),
                    listOf("Unit ID", "`1`"),
                    listOf("Timeout Ms", "`1000`"),
                    listOf("Retries", "`2`"),
                )
        }

    private fun ModbusTransportKind.configProviderSimpleName(): String =
        when (this) {
            ModbusTransportKind.RTU -> "ModbusRtuConfigProvider"
            ModbusTransportKind.TCP -> "ModbusTcpConfigProvider"
        }

    private fun ModbusTransportKind.endpointConfigSimpleName(): String =
        when (this) {
            ModbusTransportKind.RTU -> "ModbusRtuEndpointConfig"
            ModbusTransportKind.TCP -> "ModbusTcpEndpointConfig"
        }

    private fun ModbusTransportKind.configRegistrySimpleName(): String =
        when (this) {
            ModbusTransportKind.RTU -> "ModbusRtuConfigRegistry"
            ModbusTransportKind.TCP -> "ModbusTcpConfigRegistry"
        }

    private fun ModbusTransportKind.executorSimpleName(): String =
        when (this) {
            ModbusTransportKind.RTU -> "ModbusRtuExecutor"
            ModbusTransportKind.TCP -> "ModbusTcpExecutor"
        }

    private fun ModbusTransportKind.requestConfigInterfaceName(): String =
        when (this) {
            ModbusTransportKind.RTU -> "GeneratedModbusRtuRequestConfig"
            ModbusTransportKind.TCP -> "GeneratedModbusTcpRequestConfig"
        }

    private fun ModbusTransportKind.requestConfigFields(): String =
        when (this) {
            ModbusTransportKind.RTU ->
                """
    override val portPath: String? = null,
    override val unitId: Int? = null,
    override val baudRate: Int? = null,
    override val dataBits: Int? = null,
    override val stopBits: Int? = null,
    override val parity: ModbusSerialParity? = null,
    override val timeoutMs: Long? = null,
    override val retries: Int? = null,
                """.trimIndent()

            ModbusTransportKind.TCP ->
                """
    override val host: String? = null,
    override val port: Int? = null,
    override val unitId: Int? = null,
    override val timeoutMs: Long? = null,
    override val retries: Int? = null,
                """.trimIndent()
        }

    private fun renderTransportRequestSupport(transport: ModbusTransportKind): String =
        when (transport) {
            ModbusTransportKind.RTU ->
                """
private interface GeneratedModbusRtuRequestConfig {
    val portPath: String?
    val unitId: Int?
    val baudRate: Int?
    val dataBits: Int?
    val stopBits: Int?
    val parity: ModbusSerialParity?
    val timeoutMs: Long?
    val retries: Int?
}

private fun GeneratedModbusRtuRequestConfig.toEndpointConfig(defaultConfig: ModbusRtuEndpointConfig): ModbusRtuEndpointConfig =
    defaultConfig.copy(
        portPath = portPath ?: defaultConfig.portPath,
        unitId = unitId ?: defaultConfig.unitId,
        baudRate = baudRate ?: defaultConfig.baudRate,
        dataBits = dataBits ?: defaultConfig.dataBits,
        stopBits = stopBits ?: defaultConfig.stopBits,
        parity = parity ?: defaultConfig.parity,
        timeoutMs = timeoutMs ?: defaultConfig.timeoutMs,
        retries = retries ?: defaultConfig.retries,
    )
                """.trimIndent()

            ModbusTransportKind.TCP ->
                """
private interface GeneratedModbusTcpRequestConfig {
    val host: String?
    val port: Int?
    val unitId: Int?
    val timeoutMs: Long?
    val retries: Int?
}

private fun GeneratedModbusTcpRequestConfig.toEndpointConfig(defaultConfig: ModbusTcpEndpointConfig): ModbusTcpEndpointConfig =
    defaultConfig.copy(
        host = host ?: defaultConfig.host,
        port = port ?: defaultConfig.port,
        unitId = unitId ?: defaultConfig.unitId,
        timeoutMs = timeoutMs ?: defaultConfig.timeoutMs,
        retries = retries ?: defaultConfig.retries,
    )
                """.trimIndent()
        }

    private fun String.toSnakeCase(): String =
        replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
            .replace('-', '_')
            .replace(Regex("[^A-Za-z0-9_]+"), "_")
            .lowercase()

    private fun renderMarkdownTable(
        headers: List<String>,
        rows: List<List<String>>,
    ): String =
        buildString {
            appendLine(headers.joinToString(prefix = "| ", postfix = " |", separator = " | ") { escapeMarkdownTableCell(it) })
            appendLine(headers.joinToString(prefix = "| ", postfix = " |", separator = " | ") { "---" })
            rows.forEach { row ->
                appendLine(row.joinToString(prefix = "| ", postfix = " |", separator = " | ") { cell -> escapeMarkdownTableCell(cell) })
            }
        }

    private fun escapeMarkdownTableCell(text: String): String =
        text
            .replace("|", "\\|")
            .replace("\n", "<br>")

    private fun escapeComment(text: String): String = text.replace("*/", "* /")

    private fun escapeCComment(text: String): String = text.replace("*/", "* /")

    private fun ModbusServiceModel.protocolDocBaseName(): String =
        serviceId
            .trim()
            .ifBlank { interfaceSimpleName }
            .replace(Regex("[^A-Za-z0-9-]+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .lowercase()

    private fun ModbusTransportKind.dispatchFileName(): String = "modbus_${transportId}_dispatch"

    private fun ModbusTransportKind.dispatchFunctionPrefix(): String = dispatchFileName()

    private fun ModbusTransportKind.readDispatchFunctionName(functionCodeName: String): String =
        when (functionCodeName) {
            "READ_COILS" -> "${dispatchFunctionPrefix()}_read_coils"
            "READ_DISCRETE_INPUTS" -> "${dispatchFunctionPrefix()}_read_discrete_inputs"
            "READ_INPUT_REGISTERS" -> "${dispatchFunctionPrefix()}_read_input_registers"
            "READ_HOLDING_REGISTERS" -> "${dispatchFunctionPrefix()}_read_holding_registers"
            else -> error("不支持的读功能码：$functionCodeName")
        }

    private fun ModbusTransportKind.dispatchResultTypeName(): String = "${dispatchFunctionPrefix()}_command_result_t"

    private fun ModbusTransportKind.generatedKoinModuleClassName(): String =
        "GeneratedModbus${transportId.replaceFirstChar(Char::uppercase)}KoinModule"

    private fun String.asProviderMethodName(): String = asConstructorParameterName()

    private fun String.asConstructorParameterName(): String =
        replaceFirstChar { character -> character.lowercase() }
}

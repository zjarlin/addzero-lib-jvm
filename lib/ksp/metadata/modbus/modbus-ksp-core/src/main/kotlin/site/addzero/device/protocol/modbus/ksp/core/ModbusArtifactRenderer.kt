package site.addzero.device.protocol.modbus.ksp.core

/**
 * Modbus 产物模板集合。
 *
 * 这里保留实际模板渲染逻辑；
 * 具体由哪个模块暴露成 Kotlin gateway / C contract / Markdown 文档，
 * 由外层 SPI facade 决定。
 */
object ModbusArtifactTemplates {
    fun renderGatewayArtifacts(
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

    fun renderServerArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> = renderGatewayArtifacts(transport, services)

    fun renderServiceContractArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> {
        val outputPackage = "generated.modbus.${service.transport.transportId}"
        val cServiceName = service.cServiceName
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
                fileName = "${cServiceName}_bridge",
                extensionName = "h",
                content = renderBridgeHeader(service),
            ),
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = "${cServiceName}_bridge_impl",
                extensionName = "c",
                content = renderBridgeSampleSource(service),
            ),
        )
    }

    fun renderMarkdownArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> {
        val protocolFileBaseName = "${service.protocolDocBaseName()}.${service.transport.transportId}.protocol"
        return listOf(
            GeneratedArtifact(
                packageName = "generated.modbus.protocols",
                fileName = protocolFileBaseName,
                extensionName = "md",
                content = renderProtocolMarkdown(service),
            ),
        )
    }

    fun renderContractArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> =
        renderServiceContractArtifacts(service) + renderMarkdownArtifacts(service)

    fun renderTransportContractArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> {
        if (services.isEmpty()) {
            return emptyList()
        }

        val outputPackage = "generated.modbus.${transport.transportId}"
        return buildList {
            add(
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = transport.dispatchFileName(),
                extensionName = "h",
                content = renderTransportDispatchHeader(transport, services),
            ))
            add(
            GeneratedArtifact(
                packageName = outputPackage,
                fileName = transport.dispatchFileName(),
                extensionName = "c",
                content = renderTransportDispatchSource(transport, services),
            ))
            if (transport == ModbusTransportKind.RTU) {
                add(
                    GeneratedArtifact(
                        packageName = outputPackage,
                        fileName = "modbus_rtu_agile_slave_adapter",
                        extensionName = "h",
                        content = renderRtuAgileSlaveAdapterHeader(),
                    )
                )
                add(
                    GeneratedArtifact(
                        packageName = outputPackage,
                        fileName = "modbus_rtu_agile_slave_adapter",
                        extensionName = "c",
                        content = renderRtuAgileSlaveAdapterSource(),
                    )
                )
            }
        }
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
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 桥接链路：")
            appendLine(" * agile_modbus callback")
            appendLine(" *   -> ${transport.dispatchFileName()}.c")
            appendLine(" *   -> *_generated.c")
            appendLine(" *   -> *_bridge.h / 板级 bridge 实现")
            appendLine(" *")
            appendLine(" * 固件侧不需要在这里手写地址判断。")
            appendLine(" * 只需要实现每个 service 的 *_bridge_* 业务函数，然后把 RTU adapter 接到 agile_modbus。")
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

    private fun renderRtuAgileSlaveAdapterHeader(): String =
        buildString {
            appendLine("#ifndef MODBUS_RTU_AGILE_SLAVE_ADAPTER_H")
            appendLine("#define MODBUS_RTU_AGILE_SLAVE_ADAPTER_H")
            appendLine()
            appendLine("#include \"agile_modbus.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * RTU + agile_modbus 适配入口。")
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 在 freertos.c 或你的串口任务里，把 agile_modbus_slave_handle(...) 的 callback")
            appendLine(" * 替换成 generated_modbus_rtu_agile_slave_callback。")
            appendLine(" *")
            appendLine(" * 业务桥接函数不需要在这个文件里实现；")
            appendLine(" * 它们会通过 modbus_rtu_dispatch.c -> *_generated.c -> *_bridge.h 自动被调用。")
            appendLine(" */")
            appendLine()
            appendLine("int generated_modbus_rtu_agile_slave_callback(")
            appendLine("    agile_modbus_t *ctx,")
            appendLine("    struct agile_modbus_slave_info *slave_info,")
            appendLine("    const void *data")
            appendLine(");")
            appendLine()
            appendLine("#endif")
        }

    private fun renderRtuAgileSlaveAdapterSource(): String =
        buildString {
            appendLine("#include \"modbus_rtu_agile_slave_adapter.h\"")
            appendLine()
            appendLine("#include \"modbus_rtu_dispatch.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * 这个文件只做 transport/runtime 适配：")
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" * 1. 从 agile_modbus 解析请求")
            appendLine(" * 2. 调用 modbus_rtu_dispatch_*")
            appendLine(" * 3. 把 dispatch 返回值重新打包回 agile_modbus send_buf")
            appendLine(" *")
            appendLine(" * 它不承载任何板级业务逻辑。")
            appendLine(" */")
            appendLine()
            appendLine("static int generated_pack_read_coils(agile_modbus_t *ctx, struct agile_modbus_slave_info *slave_info, bool (*dispatch_fn)(uint16_t, uint16_t, bool *)) {")
            appendLine("    if (ctx == NULL || slave_info == NULL || dispatch_fn == NULL) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("    }")
            appendLine("    bool coil_values[256] = {0};")
            appendLine("    if (slave_info->nb > 256) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("    }")
            appendLine("    if (!dispatch_fn((uint16_t)slave_info->address, (uint16_t)slave_info->nb, coil_values)) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;")
            appendLine("    }")
            appendLine("    for (int i = 0; i < slave_info->nb; ++i) {")
            appendLine("        agile_modbus_slave_io_set(ctx->send_buf + slave_info->send_index, i, coil_values[i] ? 1 : 0);")
            appendLine("    }")
            appendLine("    return 0;")
            appendLine("}")
            appendLine()
            appendLine("static int generated_pack_read_registers(agile_modbus_t *ctx, struct agile_modbus_slave_info *slave_info, bool (*dispatch_fn)(uint16_t, uint16_t, uint16_t *)) {")
            appendLine("    if (ctx == NULL || slave_info == NULL || dispatch_fn == NULL) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("    }")
            appendLine("    uint16_t register_values[128] = {0};")
            appendLine("    if (slave_info->nb > 128) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("    }")
            appendLine("    if (!dispatch_fn((uint16_t)slave_info->address, (uint16_t)slave_info->nb, register_values)) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;")
            appendLine("    }")
            appendLine("    for (int i = 0; i < slave_info->nb; ++i) {")
            appendLine("        agile_modbus_slave_register_set(ctx->send_buf + slave_info->send_index, i, register_values[i]);")
            appendLine("    }")
            appendLine("    return 0;")
            appendLine("}")
            appendLine()
            appendLine("int generated_modbus_rtu_agile_slave_callback(")
            appendLine("    agile_modbus_t *ctx,")
            appendLine("    struct agile_modbus_slave_info *slave_info,")
            appendLine("    const void *data")
            appendLine(") {")
            appendLine("    (void)data;")
            appendLine("    if (ctx == NULL || slave_info == NULL || slave_info->sft == NULL) {")
            appendLine("        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("    }")
            appendLine()
            appendLine("    switch (slave_info->sft->function) {")
            appendLine("        case AGILE_MODBUS_FC_READ_COILS:")
            appendLine("            return generated_pack_read_coils(ctx, slave_info, modbus_rtu_dispatch_read_coils);")
            appendLine("        case AGILE_MODBUS_FC_READ_DISCRETE_INPUTS:")
            appendLine("            return generated_pack_read_coils(ctx, slave_info, modbus_rtu_dispatch_read_discrete_inputs);")
            appendLine("        case AGILE_MODBUS_FC_READ_INPUT_REGISTERS:")
            appendLine("            return generated_pack_read_registers(ctx, slave_info, modbus_rtu_dispatch_read_input_registers);")
            appendLine("        case AGILE_MODBUS_FC_READ_HOLDING_REGISTERS:")
            appendLine("            return generated_pack_read_registers(ctx, slave_info, modbus_rtu_dispatch_read_holding_registers);")
            appendLine("        case AGILE_MODBUS_FC_WRITE_SINGLE_COIL: {")
            appendLine("            modbus_rtu_dispatch_command_result_t result = {0};")
            appendLine("            const int accepted = modbus_rtu_dispatch_write_single_coil((uint16_t)slave_info->address, *((int *)slave_info->buf) != 0, &result);")
            appendLine("            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;")
            appendLine("        }")
            appendLine("        case AGILE_MODBUS_FC_WRITE_MULTIPLE_COILS: {")
            appendLine("            bool coil_values[256] = {0};")
            appendLine("            if (slave_info->nb > 256) {")
            appendLine("                return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("            }")
            appendLine("            for (int i = 0; i < slave_info->nb; ++i) {")
            appendLine("                coil_values[i] = agile_modbus_slave_io_get(slave_info->buf, i) != 0;")
            appendLine("            }")
            appendLine("            modbus_rtu_dispatch_command_result_t result = {0};")
            appendLine("            const int accepted = modbus_rtu_dispatch_write_multiple_coils((uint16_t)slave_info->address, (uint16_t)slave_info->nb, coil_values, &result);")
            appendLine("            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;")
            appendLine("        }")
            appendLine("        case AGILE_MODBUS_FC_WRITE_SINGLE_REGISTER: {")
            appendLine("            modbus_rtu_dispatch_command_result_t result = {0};")
            appendLine("            const int accepted = modbus_rtu_dispatch_write_single_register((uint16_t)slave_info->address, (uint16_t)(*((int *)slave_info->buf)), &result);")
            appendLine("            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;")
            appendLine("        }")
            appendLine("        case AGILE_MODBUS_FC_WRITE_MULTIPLE_REGISTERS: {")
            appendLine("            uint16_t register_values[128] = {0};")
            appendLine("            if (slave_info->nb > 128) {")
            appendLine("                return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;")
            appendLine("            }")
            appendLine("            for (int i = 0; i < slave_info->nb; ++i) {")
            appendLine("                register_values[i] = agile_modbus_slave_register_get(slave_info->buf, i);")
            appendLine("            }")
            appendLine("            modbus_rtu_dispatch_command_result_t result = {0};")
            appendLine("            const int accepted = modbus_rtu_dispatch_write_multiple_registers((uint16_t)slave_info->address, (uint16_t)slave_info->nb, register_values, &result);")
            appendLine("            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;")
            appendLine("        }")
            appendLine("        default:")
            appendLine("            return -AGILE_MODBUS_EXCEPTION_ILLEGAL_FUNCTION;")
            appendLine("    }")
            appendLine("}")
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
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"unsupported write single coil address\");")
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
                appendLine("                ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"${escapeCComment(operation.operationId)} coil quantity mismatch\");")
                appendLine("                return false;")
                appendLine("            }")
                appendLine("            ${service.cServiceName}_command_result_t service_result = {0};")
                appendLine("            const bool handled = ${operation.dispatchFunctionName(service)}(input_coils, quantity, &service_result);")
                appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, service_result.accepted, service_result.summary);")
                appendLine("            return handled;")
                appendLine("        }")
            }
            appendLine("        default:")
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"unsupported write multiple coils address\");")
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
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"unsupported write single register address\");")
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
                appendLine("                ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"${escapeCComment(operation.operationId)} register quantity mismatch\");")
                appendLine("                return false;")
                appendLine("            }")
                appendLine("            ${service.cServiceName}_command_result_t service_result = {0};")
                appendLine("            const bool handled = ${operation.dispatchFunctionName(service)}(input_registers, quantity, &service_result);")
                appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, service_result.accepted, service_result.summary);")
                appendLine("            return handled;")
                appendLine("        }")
            }
            appendLine("        default:")
            appendLine("            ${transport.dispatchFunctionPrefix()}_set_result(out_result, false, \"unsupported write registers address\");")
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
            appendLine(" * 该 gateway 由 KSP 自动生成，负责把高阶 Kotlin 接口翻译成 ${service.transport.displayName} 调用。")
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
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 该文件由 ${service.transport.displayName} KSP 自动生成。")
            appendLine(" *")
            appendLine(" * 职责：")
            appendLine(" * - 定义该 service 的 Modbus address / quantity 常量")
            appendLine(" * - 定义 request/response DTO 对应的 C struct")
            appendLine(" * - 声明 generated dispatch 入口")
            appendLine(" *")
            appendLine(" * 固件同事真正要实现的板级逻辑不在这里，")
            appendLine(" * 而是在 ${service.cServiceName}_bridge_impl.c 这类 bridge 实现文件里。")
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
                            appendLine("    /* ${escapeCComment(property.cFieldComment())} */")
                            appendLine("    ${property.cMemberDeclaration()}")
                        }
                        appendLine("} ${service.cServiceName}_${operation.operationId.toSnakeCase()}_response_t;")
                        appendLine()
                    }

                    else -> Unit
                }
                if (operation.parameters.isNotEmpty()) {
                    appendLine("typedef struct {")
                    operation.parameters.forEach { parameter ->
                        appendLine("    /* ${escapeCComment(parameter.cFieldComment())} */")
                        appendLine("    ${parameter.cMemberDeclaration()}")
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

    private fun renderBridgeHeader(service: ModbusServiceModel): String =
        buildString {
            val guard = "${service.cServiceName}_bridge_h".uppercase()
            appendLine("#ifndef $guard")
            appendLine("#define $guard")
            appendLine()
            appendLine("#include \"${service.cServiceName}_generated.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * ${service.interfaceSimpleName} bridge SPI。")
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 这是固件业务层唯一需要长期维护的 service 接口面。")
            appendLine(" *")
            appendLine(" * 集成方法：")
            appendLine(" * 1. 在你的板级/业务 .c 文件中 #include \"${service.cServiceName}_bridge.h\"")
            appendLine(" * 2. 实现下面声明的 ${service.cServiceName}_bridge_* 函数")
            appendLine(" * 3. 这些 bridge 函数负责读取真实 GPIO、寄存器、传感器状态，或处理写请求")
            appendLine(" * 4. *_generated.c 会调用这些 bridge 函数完成 DTO <-> Modbus 数据转换")
            appendLine(" *")
            appendLine(" * 桥接链路：adapter -> dispatch -> generated -> bridge implementation")
            appendLine(" *")
            appendLine(" * 不要修改 *_generated.c；")
            appendLine(" * 若要接板级逻辑，只改你自己的 ${service.cServiceName}_bridge_impl.c。")
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                appendLine("/* ${escapeCComment(operation.doc.summary)} */")
                appendLine("${operation.bridgeSignature(service)};")
                appendLine()
            }
            appendLine("#endif")
        }

    private fun renderGeneratedSource(service: ModbusServiceModel): String =
        buildString {
            appendLine("#include \"${service.cServiceName}_generated.h\"")
            appendLine("#include \"${service.cServiceName}_bridge.h\"")
            if (service.usesStringRegisters()) {
                appendLine()
                appendLine("static void ${service.cServiceName}_generated_encode_string_registers(")
                appendLine("    const char *input,")
                appendLine("    uint16_t *out_registers,")
                appendLine("    size_t register_offset,")
                appendLine("    size_t register_width")
                appendLine(") {")
                appendLine("    if (out_registers == NULL) {")
                appendLine("        return;")
                appendLine("    }")
                appendLine("    for (size_t register_index = 0; register_index < register_width; ++register_index) {")
                appendLine("        out_registers[register_offset + register_index] = 0u;")
                appendLine("    }")
                appendLine("    if (input == NULL) {")
                appendLine("        return;")
                appendLine("    }")
                appendLine("    const size_t byte_capacity = register_width * 2u;")
                appendLine("    for (size_t byte_index = 0; byte_index < byte_capacity; ++byte_index) {")
                appendLine("        const unsigned char value = (unsigned char)input[byte_index];")
                appendLine("        if (value == 0u) {")
                appendLine("            break;")
                appendLine("        }")
                appendLine("        const size_t target_register = register_offset + (byte_index / 2u);")
                appendLine("        if ((byte_index % 2u) == 0u) {")
                appendLine("            out_registers[target_register] = (uint16_t)(value << 8);")
                appendLine("        } else {")
                appendLine("            out_registers[target_register] |= (uint16_t)value;")
                appendLine("        }")
                appendLine("    }")
                appendLine("}")
                appendLine()
                appendLine("static void ${service.cServiceName}_generated_decode_string_registers(")
                appendLine("    const uint16_t *input_registers,")
                appendLine("    size_t register_offset,")
                appendLine("    size_t register_width,")
                appendLine("    char *out_text,")
                appendLine("    size_t out_capacity")
                appendLine(") {")
                appendLine("    if (out_text == NULL || out_capacity == 0u) {")
                appendLine("        return;")
                appendLine("    }")
                appendLine("    out_text[0] = '\\0';")
                appendLine("    if (input_registers == NULL) {")
                appendLine("        return;")
                appendLine("    }")
                appendLine("    const size_t byte_capacity = register_width * 2u;")
                appendLine("    const size_t text_capacity = out_capacity - 1u;")
                appendLine("    size_t written = 0u;")
                appendLine("    for (size_t byte_index = 0; byte_index < byte_capacity && written < text_capacity; ++byte_index) {")
                appendLine("        const uint16_t raw = input_registers[register_offset + (byte_index / 2u)];")
                appendLine("        const unsigned char value =")
                appendLine("            ((byte_index % 2u) == 0u) ? (unsigned char)((raw >> 8) & 0xFFu) : (unsigned char)(raw & 0xFFu);")
                appendLine("        if (value == 0u) {")
                appendLine("            break;")
                appendLine("        }")
                appendLine("        out_text[written++] = (char)value;")
                appendLine("    }")
                appendLine("    out_text[written] = '\\0';")
                appendLine("}")
            }
            appendLine()
            appendLine("/*")
            appendLine(" * generated dispatch 实现。")
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 它负责：")
            appendLine(" * - 检查 Modbus quantity / buffer 边界")
            appendLine(" * - 在 request/response struct 与 Modbus bit/register buffer 之间编解码")
            appendLine(" * - 调用 ${service.cServiceName}_bridge_* SPI 获取或提交业务数据")
            appendLine(" *")
            appendLine(" * 固件业务代码不要改这里。")
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                appendLine("${operation.generatedDispatchSignature(service)} {")
                operation.renderCDispatchBody(service).forEach { line -> appendLine("    $line") }
                appendLine("}")
                appendLine()
            }
        }

    private fun renderBridgeSampleSource(service: ModbusServiceModel): String =
        buildString {
            appendLine("#include \"${service.cServiceName}_bridge.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * bridge implementation entry.")
            appendLine(" *")
            appendLine(" * This file is the board-facing implementation entry generated from the contract.")
            appendLine(" *")
            appendLine(" * 集成方法：")
            appendLine(" * - 在下面这些 ${service.cServiceName}_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动")
            appendLine(" * - 保留 #include \"${service.cServiceName}_bridge.h\"")
            appendLine(" * - 不要修改函数签名")
            appendLine(" * - 这个文件建议放在 Core/Src/modbus 或你通过 KSP 参数指定的业务目录")
            appendLine(" *")
            appendLine(" * 不要直接修改 generated 的 *_generated.c。")
            appendLine(" * Modbus 桥接最终会自动调用这里声明的 SPI 函数。")
            appendLine(" *")
            appendLine(" * 要改哪里：")
            appendLine(" * - 只改下面这些 ${service.cServiceName}_bridge_* 函数的函数体")
            appendLine(" * - 把真实硬件读写逻辑填进去")
            appendLine(" * - 不要改函数签名，不要改 *_generated.c / *_dispatch.c / adapter")
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                appendLine("${operation.bridgeSignature(service)} {")
                appendLine("    /* 要改哪里：从这里开始补板级业务逻辑。 */")
                appendLine("    /* ${escapeCComment(operation.doc.summary)} */")
                when (operation.returnType.kind) {
                    ModbusReturnKind.DTO -> {
                        appendLine("    if (out_response == NULL) {")
                        appendLine("        return false;")
                        appendLine("    }")
                        operation.returnType.properties.forEach { property ->
                            appendLine("    ${property.renderBridgeDefaultAssignment("out_response->")}")
                        }
                        appendLine("    return true;")
                    }

                    ModbusReturnKind.COMMAND_RESULT -> {
                        appendLine("    if (out_result == NULL) {")
                        appendLine("        return false;")
                        appendLine("    }")
                        appendLine("    out_result->accepted = true;")
                        appendLine("    out_result->summary = \"TODO: bridge implementation\";")
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
                appendLine("    /* 要改哪里：到这里结束。 */")
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

    private fun ModbusOperationModel.bridgeSignature(service: ModbusServiceModel): String =
        when (returnType.kind) {
            ModbusReturnKind.DTO ->
                "bool ${bridgeFunctionName(service)}(${responseStructName(service)} *out_response)"

            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT ->
                if (isReadOperation) {
                    "bool ${bridgeFunctionName(service)}(${returnType.scalarOutType()} *out_value)"
                } else {
                    buildWriteBridgeSignature(service)
                }

            ModbusReturnKind.COMMAND_RESULT,
            ModbusReturnKind.UNIT -> buildWriteBridgeSignature(service)
        }

    private fun ModbusOperationModel.renderCDispatchBody(service: ModbusServiceModel): List<String> =
        if (isReadOperation) {
            renderReadDispatchBody(service)
        } else {
            renderWriteDispatchBody(service)
        }

    private fun ModbusOperationModel.buildWriteBridgeSignature(service: ModbusServiceModel): String {
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
        return "bool ${bridgeFunctionName(service)}($requestPart$resultPart)"
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
                        add("if (!${bridgeFunctionName(service)}(&response)) {")
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
                        add("if (!${bridgeFunctionName(service)}(&response)) {")
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

                                        ModbusValueKind.STRING -> {
                                            add("${service.cServiceName}_generated_encode_string_registers(response.${property.name.toSnakeCase()}, out_registers, ${field.registerOffset}, ${field.registerWidth});")
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
                    add("if (!${bridgeFunctionName(service)}(&value)) {")
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
                    add("if (!${bridgeFunctionName(service)}(&value)) {")
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

                        parameter.valueKind == ModbusValueKind.STRING -> {
                            add("${service.cServiceName}_generated_decode_string_registers(input_registers, ${parameter.registerOffset}, ${parameter.registerWidth}, request.${parameter.name.toSnakeCase()}, sizeof(request.${parameter.name.toSnakeCase()}));")
                        }

                        else -> {
                            add("request.${parameter.name.toSnakeCase()} = (int32_t)input_registers[${parameter.registerOffset}];")
                        }
                    }
                }
            }
            if (returnType.kind == ModbusReturnKind.COMMAND_RESULT) {
                add("return ${bridgeFunctionName(service)}(${if (parameters.isNotEmpty()) "&request, " else ""}out_result);")
            } else if (parameters.isNotEmpty()) {
                add("out_result->accepted = ${bridgeFunctionName(service)}(&request);")
                add("out_result->summary = out_result->accepted ? \"operation succeeded\" : \"operation failed\";")
                add("return out_result->accepted;")
            } else {
                add("out_result->accepted = ${bridgeFunctionName(service)}();")
                add("out_result->summary = out_result->accepted ? \"operation succeeded\" : \"operation failed\";")
                add("return out_result->accepted;")
            }
        }

    private fun ModbusOperationModel.dispatchFunctionName(service: ModbusServiceModel): String =
        "${service.cServiceName}_generated_${operationId.toSnakeCase()}"

    private fun ModbusOperationModel.bridgeFunctionName(service: ModbusServiceModel): String =
        "${service.cServiceName}_bridge_${operationId.toSnakeCase()}"

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
            ModbusValueKind.STRING -> "char"
        }

    private fun ModbusPropertyModel.cType(): String =
        when (valueKind) {
            ModbusValueKind.BOOLEAN -> "bool"
            ModbusValueKind.INT -> "int32_t"
            ModbusValueKind.STRING -> "char"
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

            ModbusValueKind.STRING ->
                "ModbusCodecSupport.decodeString(ModbusCodec.${fieldModel.codecName}, registers, ${fieldModel.registerOffset}, ${fieldModel.registerWidth})"
        }
    }

    private fun ModbusParameterModel.kotlinTypeSimpleName(): String =
        when (qualifiedType) {
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Int" -> "Int"
            "kotlin.String" -> "String"
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

            "STRING_ASCII",
            "STRING_UTF8" -> "ModbusCodecSupport.encodeString(ModbusCodec.${codecName}, ${name}, ${registerWidth}).first()"
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
                    renderRegisterPackExpression(),
                    "    .forEachIndexed { index, value -> encodedValues[${registerOffset} + index] = value }",
                )
        }

    private fun ModbusPropertyModel.kotlinTypeSimpleName(): String =
        when (qualifiedType) {
            "kotlin.Boolean" -> "Boolean"
            "kotlin.Int" -> "Int"
            "kotlin.String" -> "String"
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

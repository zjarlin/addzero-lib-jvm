package site.addzero.device.protocol.modbus.ksp.core

import java.time.LocalDate

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
        transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
        serverRouteMode: ModbusServerRouteMode = ModbusServerRouteMode.DIRECT_KTOR,
    ): List<GeneratedArtifact> {
        if (services.isEmpty()) {
            return emptyList()
        }
        val hasFirmwareWorkflow = services.any { service -> service.workflows.isNotEmpty() }
        val includeDirectKtorRoutes = serverRouteMode == ModbusServerRouteMode.DIRECT_KTOR

        val fileContent =
            buildString {
                appendLine("package ${transport.generatedPackage}")
                appendLine()
                if (includeDirectKtorRoutes) {
                    appendLine("import io.ktor.server.request.receive")
                    appendLine("import io.ktor.server.response.respond")
                    appendLine("import io.ktor.server.routing.Route")
                    appendLine("import io.ktor.server.routing.post")
                }
                appendLine("import kotlinx.serialization.Serializable")
                appendLine("import org.koin.core.annotation.Module")
                appendLine("import org.koin.core.annotation.Single")
                if (includeDirectKtorRoutes) {
                    appendLine("import org.koin.mp.KoinPlatform")
                }
                if (hasFirmwareWorkflow) {
                    appendLine("import java.util.zip.CRC32")
                }
                when (transport) {
                    ModbusTransportKind.RTU -> {
                        appendLine("import site.addzero.device.driver.modbus.rtu.DefaultModbusRtuEndpointConfig")
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
                if (hasFirmwareWorkflow) {
                    append(renderFirmwareWorkflowSupport())
                    appendLine()
                }
                append(renderTransportRequestSupport(transport))
                appendLine()
                services.forEach { service ->
                    append(renderRequestClasses(service))
                    appendLine()
                    if (transport != ModbusTransportKind.RTU) {
                        append(renderConfigProvider(service, transportDefaults))
                        appendLine()
                    }
                    append(renderGateway(service))
                    appendLine()
                }
                append(renderModule(transport, services))
                if (includeDirectKtorRoutes) {
                    appendLine()
                    append(renderRouteRegistrar(transport, services))
                }
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
        transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
        serverRouteMode: ModbusServerRouteMode = ModbusServerRouteMode.DIRECT_KTOR,
    ): List<GeneratedArtifact> = renderGatewayArtifacts(transport, services, transportDefaults, serverRouteMode)

    fun renderSpringRouteSourceArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> {
        if (services.isEmpty()) {
            return emptyList()
        }

        val basePaths = services.map(ModbusServiceModel::basePath).distinct()
        val commonBasePath = basePaths.singleOrNull()?.let { "$it/${transport.transportId}" }
        val fileContent =
            buildString {
                commonBasePath?.let { basePath ->
                    appendLine("@file:site.addzero.springktor.runtime.RequestMapping(\"${escapeKotlinString(basePath)}\")")
                    appendLine()
                }
                appendLine("package ${transport.generatedPackage}")
                appendLine()
                appendLine("import org.koin.mp.KoinPlatform")
                appendLine("import org.springframework.web.bind.annotation.PostMapping")
                appendLine("import org.springframework.web.bind.annotation.RequestBody")
                appendLine()
                appendLine("/**")
                appendLine(" * ${transport.displayName} 自动生成的 Spring2Ktor 路由源码。")
                appendLine(" *")
                appendLine(" * 这里故意保持为顶层薄适配函数，")
                appendLine(" * 直接从 Koin 取生成 gateway，避免再额外生成一层 controller bean 装配。")
                appendLine(" */")
                services.forEachIndexed { serviceIndex, service ->
                    if (serviceIndex > 0) {
                        appendLine()
                    }
                    service.operations.forEach { operation ->
                        append(renderSpringRouteFunction(transport, service, operation, commonBasePath))
                        appendLine()
                        appendLine()
                    }
                    service.workflows.forEachIndexed { workflowIndex, workflow ->
                        append(renderSpringRouteFunction(transport, service, workflow, commonBasePath))
                        if (workflowIndex != service.workflows.lastIndex) {
                            appendLine()
                            appendLine()
                        }
                    }
                }
            }

        return listOf(
            GeneratedArtifact(
                packageName = transport.generatedPackage,
                fileName = "GeneratedModbus${transport.transportId.replaceFirstChar(Char::uppercase)}SpringRoutesSource",
                extensionName = "kt",
                content = fileContent.trimEnd() + "\n",
            )
        )
    }

    fun renderKtorfitClientArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
        packageName: String,
    ): List<GeneratedArtifact> {
        if (services.isEmpty()) {
            return emptyList()
        }

        val fileContent =
            buildString {
                appendLine("package $packageName")
                appendLine()
                appendLine("import de.jensklingenberg.ktorfit.Ktorfit")
                appendLine("import de.jensklingenberg.ktorfit.http.Body")
                appendLine("import de.jensklingenberg.ktorfit.http.POST")
                appendLine("import kotlinx.serialization.Serializable")
                appendLine("import org.koin.mp.KoinPlatform")
                when (transport) {
                    ModbusTransportKind.RTU -> {
                        appendLine("import site.addzero.device.driver.modbus.rtu.ModbusSerialParity")
                    }

                    ModbusTransportKind.TCP -> Unit
                }
                appendLine()
                append(renderTransportRequestSupport(transport))
                appendLine()
                appendLine()
                services.forEach { service ->
                    append(renderRequestClasses(service))
                    appendLine()
                    append(renderKtorfitClientInterface(service))
                    appendLine()
                }
                append(renderKtorfitClientApisObject(transport, services))
            }

        return listOf(
            GeneratedArtifact(
                packageName = packageName,
                fileName = "GeneratedModbus${transport.transportId.replaceFirstChar(Char::uppercase)}KtorfitClient",
                extensionName = "kt",
                content = fileContent,
            )
        )
    }

    fun renderServiceContractArtifacts(
        service: ModbusServiceModel,
        transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
    ): List<GeneratedArtifact> {
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
                content = renderBridgeSampleSource(service, transportDefaults),
            ),
        )
    }

    fun renderMarkdownArtifacts(
        service: ModbusServiceModel,
        transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
    ): List<GeneratedArtifact> {
        val protocolFileBaseName = "${service.protocolDocBaseName()}.${service.transport.transportId}.protocol"
        return listOf(
            GeneratedArtifact(
                packageName = "generated.modbus.protocols",
                fileName = protocolFileBaseName,
                extensionName = "md",
                content = renderProtocolMarkdown(service, transportDefaults),
            ),
            GeneratedArtifact(
                packageName = "generated.modbus.${service.transport.transportId}",
                fileName = "${service.cServiceName}_bridge_sample",
                extensionName = "c",
                content = renderBridgeReferenceSampleSource(service),
            ),
        )
    }

    fun renderContractArtifacts(
        service: ModbusServiceModel,
        transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
    ): List<GeneratedArtifact> =
        renderServiceContractArtifacts(service, transportDefaults) + renderMarkdownArtifacts(service, transportDefaults)

    fun renderTransportContractArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
        transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
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

    private fun renderFirmwareWorkflowSupport(): String =
        buildString {
            appendLine("private fun generatedModbusPackU16(source: ByteArray, startIndex: Int): Int {")
            appendLine("    val high = source.getOrElse(startIndex) { 0 }.toInt() and 0xFF")
            appendLine("    val low = source.getOrElse(startIndex + 1) { 0 }.toInt() and 0xFF")
            appendLine("    return (high shl 8) or low")
            appendLine("}")
            appendLine()
            appendLine("private fun generatedModbusPackU32Be(source: ByteArray, startIndex: Int): Int {")
            appendLine("    val b0 = source.getOrElse(startIndex) { 0 }.toInt() and 0xFF")
            appendLine("    val b1 = source.getOrElse(startIndex + 1) { 0 }.toInt() and 0xFF")
            appendLine("    val b2 = source.getOrElse(startIndex + 2) { 0 }.toInt() and 0xFF")
            appendLine("    val b3 = source.getOrElse(startIndex + 3) { 0 }.toInt() and 0xFF")
            appendLine("    return (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3")
            appendLine("}")
            appendLine()
            appendLine("private fun generatedModbusCrc32(bytes: ByteArray): Int {")
            appendLine("    val crc32 = CRC32()")
            appendLine("    crc32.update(bytes)")
            appendLine("    return crc32.value.toInt()")
            appendLine("}")
        }

    private fun renderProtocolMarkdown(
        service: ModbusServiceModel,
        transportDefaults: ModbusTransportDefaults,
    ): String =
        buildString {
            appendLine("# ${service.interfaceSimpleName} 协议说明（${service.transport.displayName}）")
            appendLine()
            appendLine("## 服务概览")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("项目", "内容"),
                    rows =
                        listOf(
                            listOf("服务标识", "`${service.serviceId}`"),
                            listOf("传输方式", "`${service.transport.displayName}`"),
                            listOf("基础路径", "`${service.basePath}`"),
                            listOf("接口", "`${service.interfaceQualifiedName}`"),
                            listOf("说明", service.summary.ifBlank { "-" }),
                        ),
                ),
            )
            appendLine()
            appendLine("## 联调说明")
            appendLine()
            appendLine("- 这份文档给上位机、固件和联调同事共用。")
            appendLine("- 联调时以上表 `address`、`quantity`、`function code`、标准码值为准。")
            appendLine("- 固件侧需要实现的入口是 `${service.cServiceName}_bridge_impl.c` 里的 `${service.cServiceName}_bridge_*` 函数。")
            appendLine("- 如果 `${service.cServiceName}_bridge_impl.c` 已经存在，重新生成时不会覆盖；请对照 `${service.cServiceName}_bridge_sample.c` 查看最新模板。")
            appendLine("- 固件侧不要修改 `${service.cServiceName}_generated.c`、`${service.transport.dispatchFileName()}.c` 和 adapter。")
            appendLine("- `STRING_UTF8` 字段的 `Width` 表示寄存器个数，实际可写入字节数 = `Width * 2`。")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("生成文件", "用途"),
                    rows =
                        listOf(
                            listOf("`${service.cServiceName}_generated.h/.c`", "DTO 结构体、地址常量、寄存器/线圈编解码。"),
                            listOf("`${service.cServiceName}_bridge.h`", "给固件同事看的 SPI 头文件；声明需要实现的 bridge 函数。"),
                            listOf("`${service.cServiceName}_bridge_impl.c`", "可编辑的板级业务实现模板；只改这里。"),
                            listOf("`${service.cServiceName}_bridge_sample.c`", "只读桥接模板参考；当已有 impl 不覆盖时，用它对照最新 SPI 和注释；该文件不参与固件编译。"),
                            listOf("`${service.transport.dispatchFileName()}.h/.c`", "聚合 dispatch，负责按 address 路由到各个 service。"),
                        ),
                ),
            )
            appendLine()
            appendLine("## 仿真软件怎么填")
            appendLine()
            append(
                    renderMarkdownTable(
                        headers = listOf("项目", "填写方式"),
                        rows = renderSimulatorConnectionMarkdownRows(service.transport, transportDefaults),
                    ),
                )
            appendLine()
            appendLine("- 常见主站软件可用 `Modbus Poll`、`QModMaster`、`ModbusClientX`。")
            appendLine("- 常见从站仿真软件可用 `Modbus Slave`、`diagslave`。")
            appendLine("- 主站发请求时，功能选文档里的“标准功能码 / 标准码值”，起始地址填“地址”，点位数或寄存器数填“数量”。")
            appendLine("- 从站仿真时，先在对应数据区预置文档要求的地址范围，再用主站按同样参数发起读写。")
            appendLine("- 读操作校验返回数据是否与字段表一致；写操作校验是否返回成功，并按需要补一次读回校验。")
            appendLine()
            appendLine("## 传输默认值")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("字段", "默认值"),
                    rows = renderTransportDefaultsMarkdownRows(service.transport, transportDefaults),
                ),
            )
            appendLine()
            appendLine("## 操作总览")
            appendLine()
            append(
                renderMarkdownTable(
                    headers = listOf("操作标识", "方法", "标准功能码", "标准码值", "标准含义", "地址", "数量", "返回类型", "说明"),
                    rows = service.operations.map { operation ->
                        val functionReference = operation.functionReference()
                        listOf(
                            "`${operation.operationId}`",
                            "`${operation.methodName}`",
                            "`${operation.functionCodeName}`",
                            "`${functionReference.hexCode}`",
                            functionReference.standardMeaning,
                            "`${operation.address}`",
                            "`${operation.quantity}`",
                            operation.returnType.renderProtocolReturnSummary(),
                            operation.doc.summary.ifBlank { "-" },
                        )
                    },
                ),
            )
            if (service.workflows.isNotEmpty()) {
                appendLine()
                appendLine("## 工作流总览")
                appendLine()
                append(
                    renderMarkdownTable(
                        headers = listOf("工作流标识", "方法", "返回类型", "说明", "底层步骤"),
                        rows = service.workflows.map { workflow ->
                            listOf(
                                "`${workflow.workflowId}`",
                                "`${workflow.methodName}`",
                                workflow.returnType.renderProtocolReturnSummary(),
                                workflow.doc.summary.ifBlank { "-" },
                                workflow.renderWorkflowStepSummary(),
                            )
                        },
                    ),
                )
            }
            service.operations.forEach { operation ->
                val functionReference = operation.functionReference()
                appendLine()
                appendLine("## `${operation.operationId}`")
                appendLine()
                append(
                    renderMarkdownTable(
                        headers = listOf("项目", "内容"),
                        rows =
                            listOf(
                                listOf("方法", "`${operation.methodName}`"),
                                listOf("标准功能码", "`${operation.functionCodeName}`"),
                                listOf("标准码值", "`${functionReference.hexCode}`"),
                                listOf("标准含义", functionReference.standardMeaning),
                                listOf("地址", "`${operation.address}`"),
                                listOf("数量", "`${operation.quantity}`"),
                                listOf("返回类型", operation.returnType.renderProtocolReturnSummary()),
                                listOf("说明", operation.doc.summary.ifBlank { "-" }),
                            ),
                    ),
                )
                appendLine()
                appendLine("### 仿真软件填写")
                appendLine()
                append(
                    renderMarkdownTable(
                        headers = listOf("项目", "填写值"),
                        rows = operation.renderSimulatorRequestRows(service.transport),
                    ),
                )
                if (service.transport == ModbusTransportKind.RTU) {
                    val frameExample = operation.rtuFrameExample(unitId = transportDefaults.rtu.unitId)
                    appendLine()
                    appendLine("### RTU 报文示例")
                    appendLine()
                    appendLine("```text")
                    appendLine("SEND >>>>>>>>>> ${frameExample.requestFrameHex}")
                    appendLine("RESPONSE>>>>>>>>>> ${frameExample.responseFrameHex}")
                    appendLine("```")
                    appendLine()
                    appendLine("- ${frameExample.note}")
                    appendLine("- 请求 payload 示例：`${frameExample.requestPayloadHex}`")
                    appendLine("- 响应 payload 示例：`${frameExample.responsePayloadHex}`")
                    appendLine()
                    appendLine("### RTU 请求帧拆解")
                    appendLine()
                    append(
                        renderTransposedFrameSegmentsMarkdownTable(frameExample.requestSegments),
                    )
                }
                if (operation.parameters.isNotEmpty()) {
                    appendLine()
                    appendLine("### 参数")
                    appendLine()
                    append(
                        renderMarkdownTable(
                            headers = listOf("名称", "类型", "编码", "寄存器偏移", "位偏移", "宽度", "说明"),
                            rows = operation.parameters.map { parameter ->
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
                        ),
                    )
                }
                val stringParameterNotes = operation.renderStringSimulatorNotes()
                if (stringParameterNotes.isNotEmpty()) {
                    appendLine()
                    appendLine("### 参数里的字符串字段填写")
                    appendLine()
                    stringParameterNotes.forEach { note -> appendLine("- $note") }
                }
                if (operation.returnType.kind == ModbusReturnKind.DTO) {
                    appendLine()
                    appendLine("### 返回字段")
                    appendLine()
                    append(
                        renderMarkdownTable(
                            headers = listOf("名称", "类型", "编码", "寄存器偏移", "位偏移", "宽度", "说明"),
                            rows = operation.returnType.properties.map { property ->
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
                        ),
                    )
                    val stringReturnNotes = operation.returnType.properties.mapNotNull { property ->
                        property.renderStringSimulatorNote(operation.address)
                    }
                    if (stringReturnNotes.isNotEmpty()) {
                        appendLine()
                        appendLine("### 返回里的字符串字段填写")
                        appendLine()
                        stringReturnNotes.forEach { note -> appendLine("- $note") }
                    }
                } else {
                    val scalarStringReturnNote = operation.returnType.renderScalarStringSimulatorNote(operation.address)
                    if (scalarStringReturnNote != null) {
                        appendLine()
                        appendLine("### 返回里的字符串字段填写")
                        appendLine()
                        appendLine("- $scalarStringReturnNote")
                    }
                }
            }
            service.workflows.forEach { workflow ->
                appendLine()
                appendLine("## 工作流 `${workflow.workflowId}`")
                appendLine()
                append(
                    renderMarkdownTable(
                        headers = listOf("项目", "内容"),
                        rows =
                            listOf(
                                listOf("方法", "`${workflow.methodName}`"),
                                listOf("返回类型", workflow.returnType.renderProtocolReturnSummary()),
                                listOf("说明", workflow.doc.summary.ifBlank { "-" }),
                                listOf("底层步骤", workflow.renderWorkflowStepSummary()),
                            ),
                    ),
                )
                when (workflow.kind) {
                    ModbusWorkflowKind.FLASH_FIRMWARE -> {
                        val startOperation = service.requireOperation(workflow.startMethodName)
                        val chunkOperation = service.requireOperation(workflow.chunkMethodName)
                        val commitOperation = service.requireOperation(workflow.commitMethodName)
                        val chunkFields = chunkOperation.flashChunkPayloadFields()
                        appendLine()
                        appendLine("### 固件工作流")
                        appendLine()
                        appendLine("1. Kotlin 上位机先调用 `${startOperation.methodName}`。")
                        appendLine("2. 然后自动按 chunk payload 宽度切片并循环调用 `${chunkOperation.methodName}`。")
                        appendLine("3. 全部分片完成后调用 `${commitOperation.methodName}`。")
                        if (service.operationOrNull(workflow.resetMethodName) != null) {
                            appendLine("4. 成功提交后，Kotlin 上位机会继续调用 `${workflow.resetMethodName}`。")
                        }
                        appendLine()
                        appendLine("- CRC32: ${if (startOperation.flashStartHasCrc32()) "由上位机自动计算并通过 firmwareStart 下发" else "当前协议未声明 CRC32 字段，上位机不会下发 CRC32"}")
                        appendLine("- 单片最大字节数: `${chunkFields.sumOf { field -> field.payloadByteWidth() }}`")
                        appendLine("- 字节顺序: `U16/U32_BE` payload 均按大端顺序把 `bytes` 填入寄存器窗口。")
                        appendLine("- C 侧职责：`begin` 初始化烧录会话，`chunk` 写分片，`commit` 完成提交，`reset` 负责板级重启。")
                    }
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
                appendLine("#include \"${service.cServiceName}/${service.cServiceName}_generated.h\"")
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
            appendLine("#include \"transport/${transport.dispatchFileName()}.h\"")
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
            appendLine("#include \"transport/modbus_rtu_agile_slave_adapter.h\"")
            appendLine()
            appendLine("#include \"transport/modbus_rtu_dispatch.h\"")
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
            service.workflows.forEach { workflow ->
                appendLine("/**")
                appendLine(" * ${escapeComment(workflow.doc.summary)}")
                appendLine(" */")
                appendLine("@Serializable")
                appendLine("data class ${workflow.requestClassName}(")
                appendLine(service.transport.requestConfigFields())
                appendLine("    /** ${escapeComment(workflow.doc.parameterDocs[workflow.bytesParameterName].orEmpty().ifBlank { "待烧录的完整固件字节数组。" })} */")
                appendLine("    val ${workflow.bytesParameterName}: ByteArray")
                appendLine(") : ${service.transport.requestConfigInterfaceName()}")
                appendLine()
            }
        }

    private fun renderConfigProvider(
        service: ModbusServiceModel,
        transportDefaults: ModbusTransportDefaults,
    ): String =
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
                        "        DefaultModbusRtuEndpointConfig(portPath = \"${escapeKotlinString(transportDefaults.rtu.portPath)}\", unitId = ${transportDefaults.rtu.unitId}, baudRate = ${transportDefaults.rtu.baudRate}, dataBits = ${transportDefaults.rtu.dataBits}, stopBits = ${transportDefaults.rtu.stopBits}, parity = ${transportDefaults.rtu.parity.renderParityEnumLiteral()}, timeoutMs = ${transportDefaults.rtu.timeoutMs}, retries = ${transportDefaults.rtu.retries})"

                    ModbusTransportKind.TCP ->
                        "        ModbusTcpEndpointConfig(serviceId = serviceId, host = \"${escapeKotlinString(transportDefaults.tcp.host)}\", port = ${transportDefaults.tcp.port}, unitId = ${transportDefaults.tcp.unitId}, timeoutMs = ${transportDefaults.tcp.timeoutMs}, retries = ${transportDefaults.tcp.retries})"
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
            when (service.transport) {
                ModbusTransportKind.RTU -> {
                    appendLine(
                        "class ${service.gatewayClassName}(" +
                            "private val configuredDefaultConfig: ${service.transport.endpointConfigSimpleName()}, " +
                            "private val executor: ${service.transport.executorSimpleName()}" +
                            ") : ${service.interfaceQualifiedName} {",
                    )
                    appendLine("    fun defaultConfig(): ${service.transport.endpointConfigSimpleName()} = configuredDefaultConfig")
                }

                ModbusTransportKind.TCP -> {
                    appendLine(
                        "class ${service.gatewayClassName}(" +
                            "private val configRegistry: ${service.transport.configRegistrySimpleName()}, " +
                            "private val executor: ${service.transport.executorSimpleName()}" +
                            ") : ${service.interfaceQualifiedName} {",
                    )
                    appendLine("    fun defaultConfig(): ${service.transport.endpointConfigSimpleName()} = configRegistry.require(\"${service.serviceId}\")")
                }
            }
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
                        appendLine("        return ModbusCodecSupport.decodeInt(ModbusCodec.${operation.returnType.codecName}, registers, 0)")

                    ModbusReturnKind.STRING ->
                        appendLine(
                            "        return ModbusCodecSupport.decodeString(" +
                                "ModbusCodec.${operation.returnType.codecName}, registers, 0, ${operation.returnType.registerWidth})",
                        )

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
            service.workflows.forEach { workflow ->
                append(renderWorkflowGatewayMethods(service, workflow))
                appendLine()
            }
            appendLine("}")
        }

    private fun renderKtorfitClientInterface(service: ModbusServiceModel): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${escapeComment(service.summary)}")
            appendLine(" *")
            appendLine(" * 该 Ktorfit client 由 Modbus KSP 自动生成，请勿手写。")
            appendLine(" */")
            appendLine("interface ${service.ktorfitApiInterfaceName()} {")
            service.operations.forEach { operation ->
                appendLine("    /** ${escapeComment(operation.doc.summary)} */")
                appendLine("    @POST(\"${service.httpRoutePath(operation.operationId)}\")")
                appendLine("    suspend fun ${operation.methodName}(")
                appendLine("        @Body request: ${operation.requestClassName},")
                appendLine("    ): ${operation.returnType.renderKotlinType()}")
                appendLine()
            }
            service.workflows.forEach { workflow ->
                appendLine("    /** ${escapeComment(workflow.doc.summary)} */")
                appendLine("    @POST(\"${service.httpRoutePath(workflow.workflowId)}\")")
                appendLine("    suspend fun ${workflow.methodName}(")
                appendLine("        @Body request: ${workflow.requestClassName},")
                appendLine("    ): ${workflow.returnType.renderKotlinType()}")
                appendLine()
            }
            appendLine("}")
        }

    private fun renderKtorfitClientApisObject(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String =
        buildString {
            appendLine("/**")
            appendLine(" * Modbus Ktorfit client 聚合入口。")
            appendLine(" */")
            appendLine("object GeneratedModbus${transport.transportId.replaceFirstChar(Char::uppercase)}Apis {")
            appendLine("    private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()")
            services.forEach { service ->
                appendLine()
                appendLine("    val ${service.ktorfitApiPropertyName()}")
                appendLine("        get() = ktorfit().create${service.ktorfitApiInterfaceName()}()")
            }
            appendLine("}")
        }

    private fun renderWorkflowGatewayMethods(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
    ): String =
        buildString {
            when (workflow.kind) {
                ModbusWorkflowKind.FLASH_FIRMWARE -> {
                    appendLine("    override suspend fun ${workflow.methodName}(${workflow.bytesParameterName}: ByteArray): ${workflow.returnType.renderKotlinType()} =")
                    appendLine("        ${workflow.methodName}(config = null, ${workflow.bytesParameterName} = ${workflow.bytesParameterName})")
                    appendLine()
                    appendLine("    /**")
                    appendLine("     * ${escapeComment(workflow.doc.summary)}")
                    appendLine("     *")
                    appendLine("     * 这是由 `${workflow.methodName}(bytes)` 高层工作流展开出来的自动实现。")
                    appendLine("     * 业务层无需手写 CRC32、分片切片和寄存器 packing。")
                    appendLine("     */")
                    appendLine(
                        "    suspend fun ${workflow.methodName}(" +
                            "config: ${service.transport.endpointConfigSimpleName()}? = null, " +
                            "${workflow.bytesParameterName}: ByteArray" +
                            "): ${workflow.returnType.renderKotlinType()} {",
                    )
                    workflow.renderFirmwareWorkflowExecution(service).forEach { line ->
                        appendLine("        $line")
                    }
                    appendLine("    }")
                }
            }
        }

    private fun renderModule(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${transport.displayName} 自动生成的 Koin 模块。")
            appendLine(" *")
            when (transport) {
                ModbusTransportKind.RTU ->
                    appendLine(" * 统一收口生成出来的网关；默认 RTU 配置由业务自己通过 Koin 提供。")

                ModbusTransportKind.TCP ->
                    appendLine(" * 统一收口生成出来的默认配置提供器、配置注册表与网关。")
            }
            appendLine(" */")
            appendLine("@Module")
            appendLine("class ${transport.generatedKoinModuleClassName()} {")
            when (transport) {
                ModbusTransportKind.RTU -> {
                    services.forEach { service ->
                        appendLine("    @Single")
                        appendLine("    fun ${service.gatewayClassName.asProviderMethodName()}(")
                        appendLine("        defaultConfig: ${transport.endpointConfigSimpleName()},")
                        appendLine("        executor: ${transport.executorSimpleName()},")
                        appendLine("    ): ${service.gatewayClassName} = ${service.gatewayClassName}(defaultConfig, executor)")
                        appendLine()
                        appendLine("    @Single")
                        appendLine("    fun ${service.interfaceSimpleName.asProviderMethodName()}(")
                        appendLine("        gateway: ${service.gatewayClassName},")
                        appendLine("    ): ${service.interfaceQualifiedName} = gateway")
                        appendLine()
                    }
                }

                ModbusTransportKind.TCP -> {
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
                        appendLine("    @Single")
                        appendLine("    fun ${service.interfaceSimpleName.asProviderMethodName()}(")
                        appendLine("        gateway: ${service.gatewayClassName},")
                        appendLine("    ): ${service.interfaceQualifiedName} = gateway")
                        appendLine()
                    }
                }
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
                service.workflows.forEach { workflow ->
                    val routePath = "${service.basePath}/${transport.transportId}/${service.serviceId}/${workflow.workflowId}"
                    appendLine("    post(\"$routePath\") {")
                    appendLine("        val request = call.receive<${workflow.requestClassName}>()")
                    appendLine("        val gateway = KoinPlatform.getKoin().get<${service.gatewayClassName}>()")
                    appendLine("        val config = request.toEndpointConfig(gateway.defaultConfig())")
                    appendLine("        call.respond(gateway.${workflow.methodName}(config = config, ${workflow.bytesParameterName} = request.${workflow.bytesParameterName}))")
                    appendLine("    }")
                }
            }
            appendLine("}")
        }

    private fun renderSpringRouteFunction(
        transport: ModbusTransportKind,
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        commonBasePath: String?,
    ): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${escapeComment(operation.doc.summary)}")
            appendLine(" *")
            appendLine(" * 这里直接从 Koin 解析 gateway，避免再额外生成 controller bean 装配。")
            appendLine(" */")
            appendLine("@PostMapping(\"${escapeKotlinString(service.springRoutePath(operation.operationId, commonBasePath))}\")")
            appendLine("suspend fun ${service.springRouteFunctionName(transport, operation.methodName)}(")
            appendLine("    @RequestBody request: ${operation.requestClassName},")
            appendLine("): ${operation.returnType.renderKotlinType()} {")
            appendLine("    val gateway = KoinPlatform.getKoin().get<${service.gatewayClassName}>()")
            appendLine("    val config = request.toEndpointConfig(gateway.defaultConfig())")
            appendLine("    return gateway.${operation.methodName}(config = config${operation.renderGatewayArguments(valuePrefix = "request.")})")
            appendLine("}")
        }

    private fun renderSpringRouteFunction(
        transport: ModbusTransportKind,
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        commonBasePath: String?,
    ): String =
        buildString {
            appendLine("/**")
            appendLine(" * ${escapeComment(workflow.doc.summary)}")
            appendLine(" *")
            appendLine(" * 这里直接从 Koin 解析 gateway，避免再额外生成 controller bean 装配。")
            appendLine(" */")
            appendLine("@PostMapping(\"${escapeKotlinString(service.springRoutePath(workflow.workflowId, commonBasePath))}\")")
            appendLine("suspend fun ${service.springRouteFunctionName(transport, workflow.methodName)}(")
            appendLine("    @RequestBody request: ${workflow.requestClassName},")
            appendLine("): ${workflow.returnType.renderKotlinType()} {")
            appendLine("    val gateway = KoinPlatform.getKoin().get<${service.gatewayClassName}>()")
            appendLine("    val config = request.toEndpointConfig(gateway.defaultConfig())")
            appendLine("    return gateway.${workflow.methodName}(config = config, ${workflow.bytesParameterName} = request.${workflow.bytesParameterName})")
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
            if (service.workflows.isNotEmpty()) {
                appendLine(" * - 标注高层 workflow 对应的低层 begin/chunk/commit/reset SPI")
            }
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
                        if (operation.returnType.docSummary.isNotBlank()) {
                            appendLine("/* ${escapeCComment(operation.returnType.docSummary)} */")
                        }
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
                    appendLine("/* ${escapeCComment(operation.doc.summary)} 请求参数。 */")
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
                operation.generatedFunctionCommentLines(service).forEach(::appendLine)
                appendLine("${operation.generatedDispatchSignature(service)};")
                appendLine()
            }
            appendLine("#endif")
        }

    private fun renderBridgeReferenceSampleSource(service: ModbusServiceModel): String {
        val updatedDate = LocalDate.now().toString()
        val bridgeSource = renderBridgeSampleSource(service, ModbusTransportDefaults()).trimStart()
        return buildString {
            appendLine("/*")
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 参考模板：${service.interfaceSimpleName} / ${service.transport.displayName}")
            appendLine(" * 更新日期：$updatedDate")
            appendLine(" *")
            appendLine(" * 用途：")
            appendLine(" * - 当 ${service.cServiceName}_bridge_impl.c 已经存在时，KSP 不会覆盖那个正式实现文件。")
            appendLine(" * - 这份 sample 只用于对照最新 SPI、函数签名、参数说明和注释。")
            appendLine(" * - 需要更新时，只把新增函数或注释片段复制到你自己的 ${service.cServiceName}_bridge_impl.c。")
            appendLine(" *")
            appendLine(" * 编译约定：")
            appendLine(" * - 该文件输出到 Docs/generated/modbus/...，不参与固件编译。")
            appendLine(" */")
            appendLine()
            append(bridgeSource)
        }
    }

    private fun renderBridgeHeader(service: ModbusServiceModel): String =
        buildString {
            val guard = "${service.cServiceName}_bridge_h".uppercase()
            appendLine("#ifndef $guard")
            appendLine("#define $guard")
            appendLine()
            appendLine("#include \"${service.cServiceName}/${service.cServiceName}_generated.h\"")
            appendLine()
            appendLine("/*")
            appendLine(" * ${service.interfaceSimpleName} bridge SPI。")
            appendLine(" * 请勿手动修改此文件。")
            appendLine(" *")
            appendLine(" * 这是固件业务层唯一需要长期维护的 service 接口面。")
            appendLine(" *")
            appendLine(" * 集成方法：")
            appendLine(" * 1. 在你的板级/业务 .c 文件中 #include \"${service.cServiceName}/${service.cServiceName}_bridge.h\"")
            appendLine(" * 2. 实现下面声明的 ${service.cServiceName}_bridge_* 函数")
            appendLine(" * 3. 这些 bridge 函数负责读取真实 GPIO、寄存器、传感器状态，或处理写请求")
            appendLine(" * 4. *_generated.c 会调用这些 bridge 函数完成 DTO <-> Modbus 数据转换")
            if (service.workflows.isNotEmpty()) {
                appendLine(" *")
                service.workflows.forEach { workflow ->
                    appendLine(" * 高层工作流：${workflow.methodName}(bytes)")
                    appendLine(" * - Kotlin 上位机会自动拆成 ${workflow.startMethodName} -> ${workflow.chunkMethodName} -> ${workflow.commitMethodName}${if (workflow.resetMethodName != null) " -> ${workflow.resetMethodName}" else ""}")
                    appendLine(" * - C 同事只需要实现下面这些低层 bridge SPI。")
                }
            }
            appendLine(" *")
            appendLine(" * 桥接链路：adapter -> dispatch -> generated -> bridge implementation")
            appendLine(" *")
            appendLine(" * 不要修改 *_generated.c；")
            appendLine(" * 若要接板级逻辑，只改你自己的 ${service.cServiceName}_bridge_impl.c。")
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                operation.bridgeFunctionCommentLines(service).forEach(::appendLine)
                appendLine("${operation.bridgeSignature(service)};")
                appendLine()
            }
            appendLine("#endif")
        }

    private fun renderGeneratedSource(service: ModbusServiceModel): String =
        buildString {
            appendLine("#include \"${service.cServiceName}/${service.cServiceName}_generated.h\"")
            appendLine("#include \"${service.cServiceName}/${service.cServiceName}_bridge.h\"")
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
                operation.generatedFunctionCommentLines(service).forEach(::appendLine)
                appendLine("${operation.generatedDispatchSignature(service)} {")
                operation.renderCDispatchBody(service).forEach { line -> appendLine("    $line") }
                appendLine("}")
                appendLine()
            }
        }

    private fun renderBridgeSampleSource(
        service: ModbusServiceModel,
        transportDefaults: ModbusTransportDefaults,
    ): String =
        buildString {
            appendLine("#include \"${service.cServiceName}/${service.cServiceName}_bridge.h\"")
            if (service.usesStringRegisters()) {
                appendLine("#include <string.h>")
            }
            appendLine()
            if (service.usesStringRegisters()) {
                appendLine("static void ${service.cServiceName}_bridge_copy_text(char *out_text, size_t out_capacity, const char *input) {")
                appendLine("    if (out_text == NULL || out_capacity == 0u) {")
                appendLine("        return;")
                appendLine("    }")
                appendLine("    if (input == NULL) {")
                appendLine("        out_text[0] = '\\0';")
                appendLine("        return;")
                appendLine("    }")
                appendLine("    strncpy(out_text, input, out_capacity - 1u);")
                appendLine("    out_text[out_capacity - 1u] = '\\0';")
                appendLine("}")
                appendLine()
            }
            appendLine("/*")
            appendLine(" * bridge implementation entry.")
            appendLine(" *")
            appendLine(" * This file is the board-facing implementation entry generated from the contract.")
            appendLine(" *")
            appendLine(" * 集成方法：")
            appendLine(" * - 在下面这些 ${service.cServiceName}_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动")
            appendLine(" * - 保留 #include \"${service.cServiceName}/${service.cServiceName}_bridge.h\"")
            appendLine(" * - 不要修改函数签名")
            appendLine(" * - 这个文件建议放在 Core/Src/modbus/<transport>/<service>，例如 Core/Src/modbus/rtu/${service.cServiceName}；也可以放在你通过 KSP 参数指定的业务目录")
            if (service.workflows.isNotEmpty()) {
                appendLine(" * - 如果这个 service 包含高层烧录工作流，Kotlin 上位机会自动计算 CRC32、切片并顺序调用 begin/chunk/commit/reset")
            }
            appendLine(" *")
            appendLine(" * 不要直接修改 generated 的 *_generated.c。")
            appendLine(" * Modbus 桥接最终会自动调用这里声明的 SPI 函数。")
            appendLine(" *")
            appendLine(" * 要改哪里：")
            appendLine(" * - 只改下面这些 ${service.cServiceName}_bridge_* 函数的函数体")
            appendLine(" * - 把真实硬件读写逻辑填进去")
            appendLine(" * - 不要改函数签名，不要改 *_generated.c / *_dispatch.c / adapter")
            if (service.usesStringRegisters()) {
                appendLine(" * - 字符串输出请直接调用 ${service.cServiceName}_bridge_copy_text(...)，不要手写多字符 char 赋值")
            }
            appendLine(" */")
            appendLine()
            service.operations.forEach { operation ->
                operation.bridgeFunctionCommentLines(service).forEach(::appendLine)
                appendLine("${operation.bridgeSignature(service)} {")
                appendLine("    /* 要改哪里：从这里开始补板级业务逻辑。 */")
                appendLine("    /* ${escapeCComment(operation.doc.summary)} */")
                if (operation.parameters.isNotEmpty()) {
                    appendLine("    /* 输入参数：")
                    operation.parameters.forEach { parameter ->
                        appendLine("     * - request->${parameter.name.toSnakeCase()}: ${escapeCComment(parameter.doc)}")
                    }
                    appendLine("     */")
                }
                when (operation.returnType.kind) {
                    ModbusReturnKind.DTO -> {
                        appendLine("    if (out_response == NULL) {")
                            appendLine("        return false;")
                        appendLine("    }")
                        operation.returnType.properties.forEach { property ->
                            property.renderBridgeDefaultAssignmentLines(
                                prefix = "out_response->",
                                bridgeCopyHelperName = "${service.cServiceName}_bridge_copy_text",
                            ).forEach { line ->
                                appendLine("    $line")
                            }
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

                    ModbusReturnKind.STRING -> {
                        appendLine("    if (out_value == NULL || out_capacity == 0u) {")
                        appendLine("        return false;")
                        appendLine("    }")
                        appendLine("    /* 返回值字符串：codec=${operation.returnType.codecName}，寄存器宽度=${operation.returnType.registerWidth}，最多 ${operation.returnType.stringByteCapacity()} 个字节，out_capacity 包含结尾 '\\0'。 */")
                        appendLine("    /* 示例：${service.cServiceName}_bridge_copy_text(out_value, out_capacity, \"XXXXXXXX-XXXXX\"); */")
                        appendLine("    ${service.cServiceName}_bridge_copy_text(out_value, out_capacity, \"\");")
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

    private fun ModbusWorkflowModel.renderFirmwareWorkflowExecution(service: ModbusServiceModel): List<String> {
        val startOperation = service.requireOperation(startMethodName)
        val chunkOperation = service.requireOperation(chunkMethodName)
        val commitOperation = service.requireOperation(commitMethodName)
        val resetOperation = service.operationOrNull(resetMethodName)
        val payloadFields = chunkOperation.flashChunkPayloadFields()
        val payloadBytesPerChunk = payloadFields.sumOf { field -> field.payloadByteWidth() }
        val hasCrc32 = startOperation.flashStartHasCrc32()
        return buildList {
            add("require(${bytesParameterName}.isNotEmpty()) { \"flashFirmware bytes must not be empty\" }")
            add("val resolvedConfig = resolveConfig(config)")
            add("val totalBytes = ${bytesParameterName}.size")
            if (hasCrc32) {
                add("val firmwareCrc32 = generatedModbusCrc32(${bytesParameterName})")
            }
            add("val maxPayloadBytesPerChunk = $payloadBytesPerChunk")
            add("val totalChunks = (totalBytes + maxPayloadBytesPerChunk - 1) / maxPayloadBytesPerChunk")
            add(
                buildString {
                    append("val startResult = ${startOperation.methodName}(config = resolvedConfig, ")
                    append(startOperation.flashStartCallArguments(hasCrc32))
                    append(")")
                },
            )
            add("check(startResult.accepted) { startResult.summary }")
            add("for (sequence in 0 until totalChunks) {")
            add("    val chunkStart = sequence * maxPayloadBytesPerChunk")
            add("    val chunkEnd = minOf(chunkStart + maxPayloadBytesPerChunk, totalBytes)")
            add("    val chunkBytes = ${bytesParameterName}.copyOfRange(chunkStart, chunkEnd)")
            add("    var payloadCursor = 0")
            payloadFields.forEach { field ->
                val packExpression =
                    when (field.codecName) {
                        "U16" -> "generatedModbusPackU16(chunkBytes, payloadCursor)"
                        "U32_BE" -> "generatedModbusPackU32Be(chunkBytes, payloadCursor)"
                        else -> error("Unsupported firmware chunk payload codec: ${field.codecName}")
                    }
                add("    val ${field.name} = $packExpression")
                add("    payloadCursor += ${field.payloadByteWidth()}")
            }
            add(
                buildString {
                    append("    val chunkResult = ${chunkOperation.methodName}(config = resolvedConfig, ")
                    append(chunkOperation.flashChunkCallArguments(payloadFields))
                    append(")")
                },
            )
            add("    check(chunkResult.accepted) { chunkResult.summary }")
            add("}")
            add("val commitResult = ${commitOperation.methodName}(config = resolvedConfig, ${commitOperation.flashCommitParameterName()} = totalChunks)")
            add("check(commitResult.accepted) { commitResult.summary }")
            if (resetOperation != null) {
                val resetParameterName = resetOperation.flashResetTriggerParameterName()
                add("val resetResult = ${resetOperation.methodName}(config = resolvedConfig, $resetParameterName = true)")
                add("check(resetResult.accepted) { resetResult.summary }")
            }
            add("val resetIssued = ${if (resetOperation != null) "true" else "false"}")
            add("return ${returnType.qualifiedName}(")
            add("    accepted = true,")
            add("    summary = \"flash workflow completed\",")
            add("    totalBytes = totalBytes,")
            add("    totalChunks = totalChunks,")
            add("    crc32 = ${if (hasCrc32) "firmwareCrc32" else "null"},")
            add("    resetIssued = resetIssued,")
            add(")")
        }
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

            ModbusReturnKind.STRING ->
                if (isReadOperation) {
                    "bool ${bridgeFunctionName(service)}(char *out_value, size_t out_capacity)"
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

            ModbusReturnKind.STRING ->
                buildList {
                    add("if (out_registers == NULL || register_count < ${quantity}) {")
                    add("    return false;")
                    add("}")
                    add("char value[${returnType.stringCharCapacity()}] = {0};")
                    add("if (!${bridgeFunctionName(service)}(value, sizeof(value))) {")
                    add("    return false;")
                    add("}")
                    add("${service.cServiceName}_generated_encode_string_registers(value, out_registers, 0u, ${returnType.registerWidth});")
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

    private fun ModbusServiceModel.requireOperation(methodName: String): ModbusOperationModel =
        operationOrNull(methodName) ?: error("Missing workflow operation: $methodName in ${interfaceQualifiedName}")

    private fun ModbusServiceModel.operationOrNull(methodName: String?): ModbusOperationModel? =
        methodName?.let { expected -> operations.firstOrNull { operation -> operation.methodName == expected } }

    private fun ModbusWorkflowModel.renderWorkflowStepSummary(): String =
        buildString {
            append("`${startMethodName}` -> `${chunkMethodName}` -> `${commitMethodName}`")
            if (resetMethodName != null) {
                append(" -> `${resetMethodName}`")
            }
        }

    private fun ModbusServiceModel.workflowNotesFor(operation: ModbusOperationModel): List<String> =
        workflows
            .filter { workflow ->
                operation.methodName in
                    setOf(
                        workflow.startMethodName,
                        workflow.chunkMethodName,
                        workflow.commitMethodName,
                        workflow.resetMethodName,
                    )
            }.flatMap { workflow ->
                when (workflow.kind) {
                    ModbusWorkflowKind.FLASH_FIRMWARE ->
                        listOf(
                            "属于高层 ${workflow.methodName}(bytes) 工作流的低层步骤。",
                            "Kotlin 上位机会自动负责整包 CRC32、chunk slicing 和顺序调度。",
                        )
                }
            }

    private fun ModbusOperationModel.generatedFunctionCommentLines(service: ModbusServiceModel): List<String> =
        cFunctionCommentLines(
            summary = doc.summary,
            notes = service.workflowNotesFor(this),
            params =
                when {
                    isReadOperation && usesCoilBits ->
                        listOf(
                            "out_coils" to "输出 Modbus 线圈缓冲区。",
                            "coil_count" to "out_coils 可写入的线圈数量。",
                        )
                    isReadOperation ->
                        listOf(
                            "out_registers" to "输出 Modbus 寄存器缓冲区。",
                            "register_count" to "out_registers 可写入的寄存器数量。",
                        )
                    usesCoilBits ->
                        listOf(
                            "input_coils" to "输入 Modbus 线圈缓冲区。",
                            "coil_count" to "input_coils 中有效的线圈数量。",
                            "out_result" to "命令处理结果输出。",
                        )
                    else ->
                        listOf(
                            "input_registers" to "输入 Modbus 寄存器缓冲区。",
                            "register_count" to "input_registers 中有效的寄存器数量。",
                            "out_result" to "命令处理结果输出。",
                        )
                },
        )

    private fun ModbusOperationModel.bridgeFunctionCommentLines(service: ModbusServiceModel): List<String> =
        cFunctionCommentLines(
            summary = doc.summary,
            notes = service.workflowNotesFor(this),
            params =
                when {
                    isReadOperation && returnType.kind == ModbusReturnKind.DTO ->
                        listOf("out_response" to "${responseStructName(service)} 输出对象。")
                    isReadOperation && returnType.kind == ModbusReturnKind.STRING ->
                        listOf(
                            "out_value" to "输出字符串缓冲区。",
                            "out_capacity" to "out_value 的字节容量，包含结尾 '\\0'。",
                        )
                    isReadOperation ->
                        listOf("out_value" to "输出读取到的标量值。")
                    parameters.isNotEmpty() && returnType.kind == ModbusReturnKind.COMMAND_RESULT ->
                        listOf(
                            "request" to "${requestStructName(service)} 输入参数对象。",
                            "out_result" to "命令处理结果输出。",
                        )
                    parameters.isNotEmpty() ->
                        listOf("request" to "${requestStructName(service)} 输入参数对象。")
                    returnType.kind == ModbusReturnKind.COMMAND_RESULT ->
                        listOf("out_result" to "命令处理结果输出。")
                    else -> emptyList()
                },
        )

    private fun cFunctionCommentLines(
        summary: String,
        notes: List<String> = emptyList(),
        params: List<Pair<String, String>>,
    ): List<String> =
        buildList {
            add("/*")
            add(" * ${escapeCComment(summary)}")
            notes.forEach { note ->
                add(" * ${escapeCComment(note)}")
            }
            if (params.isNotEmpty()) {
                add(" *")
                add(" * 参数：")
                params.forEach { (name, doc) ->
                    add(" * - $name: ${escapeCComment(doc)}")
                }
            }
            add(" */")
        }

    private fun ModbusOperationModel.flashStartHasCrc32(): Boolean =
        parameters.any { parameter -> parameter.name == "crc32" }

    private fun ModbusOperationModel.flashChunkPayloadFields(): List<ModbusParameterModel> =
        parameters
            .filter { parameter -> parameter.name != "sequence" && parameter.name != "usedBytes" }
            .sortedBy(ModbusParameterModel::registerOffset)

    private fun ModbusParameterModel.payloadByteWidth(): Int = registerWidth * 2

    private fun ModbusOperationModel.flashStartCallArguments(hasCrc32: Boolean): String =
        buildString {
            append("totalBytes = totalBytes")
            if (hasCrc32) {
                append(", crc32 = firmwareCrc32")
            }
        }

    private fun ModbusOperationModel.flashChunkCallArguments(payloadFields: List<ModbusParameterModel>): String =
        buildString {
            append("sequence = sequence, usedBytes = chunkBytes.size")
            payloadFields.forEach { field ->
                append(", ${field.name} = ${field.name}")
            }
        }

    private fun ModbusOperationModel.flashCommitParameterName(): String =
        parameters.firstOrNull { parameter -> parameter.name == "totalChunks" }?.name
            ?: error("Missing totalChunks parameter on ${methodName}")

    private fun ModbusOperationModel.flashResetTriggerParameterName(): String =
        parameters.firstOrNull { parameter -> parameter.name == "trigger" }?.name
            ?: error("Missing trigger parameter on ${methodName}")

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
            ModbusReturnKind.STRING -> "char"
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
            ModbusReturnKind.STRING -> "`String`"
            ModbusReturnKind.COMMAND_RESULT -> "`ModbusCommandResult`"
            ModbusReturnKind.DTO -> "`${simpleName}`"
        }

    private fun ModbusParameterModel.renderRegisterPackExpression(): String =
        when (valueKind) {
            ModbusValueKind.STRING -> "ModbusCodecSupport.encodeString(ModbusCodec.${codecName}, ${name}, ${registerWidth})"
            else -> "ModbusCodecSupport.encodeValue(ModbusCodec.${codecName}, ${name}.toString())"
        }

    private fun ModbusParameterModel.cMemberDeclaration(): String =
        when (valueKind) {
            ModbusValueKind.STRING -> "char ${name.toSnakeCase()}[${stringCharCapacity()}];"
            else -> "${cType()} ${name.toSnakeCase()};"
        }

    private fun ModbusPropertyModel.cMemberDeclaration(): String =
        when (valueKind) {
            ModbusValueKind.STRING -> "char ${name.toSnakeCase()}[${stringCharCapacity()}];"
            else -> "${cType()} ${name.toSnakeCase()};"
        }

    private fun ModbusParameterModel.cFieldComment(): String =
        when (valueKind) {
            ModbusValueKind.STRING -> "${doc} codec=$codecName registers=$registerWidth charCapacity=${stringCharCapacity()}。"
            else -> doc
        }

    private fun ModbusPropertyModel.cFieldComment(): String =
        when (valueKind) {
            ModbusValueKind.STRING -> "${doc} codec=${field?.codecName.orEmpty()} registers=${field?.registerWidth ?: 0} charCapacity=${stringCharCapacity()}。"
            else -> doc
        }

    private fun ModbusPropertyModel.renderBridgeDefaultAssignmentLines(
        prefix: String,
        bridgeCopyHelperName: String,
    ): List<String> =
        when (valueKind) {
            ModbusValueKind.STRING ->
                listOf(
                    "/* ${name.toSnakeCase()} 字符串：codec=${field?.codecName.orEmpty()}，寄存器宽度=${field?.registerWidth ?: 0}，最多 ${stringByteCapacity()} 个字节，缓冲区容量 ${stringCharCapacity()}（含 '\\0'）。 */",
                    "/* 示例：",
                    " * ${bridgeCopyHelperName}(${prefix}${name.toSnakeCase()}, sizeof(${prefix}${name.toSnakeCase()}), \"XXXXXXXX-XXXXX\");",
                    " */",
                    "${bridgeCopyHelperName}(${prefix}${name.toSnakeCase()}, sizeof(${prefix}${name.toSnakeCase()}), \"\");",
                )
            else -> listOf("${prefix}${name.toSnakeCase()} = 0;")
        }

    private fun ModbusParameterModel.stringCharCapacity(): Int = (registerWidth * 2) + 1

    private fun ModbusPropertyModel.stringCharCapacity(): Int = ((field?.registerWidth ?: 0) * 2) + 1

    private fun ModbusPropertyModel.stringByteCapacity(): Int = (field?.registerWidth ?: 0) * 2

    private fun ModbusReturnTypeModel.stringByteCapacity(): Int = registerWidth * 2

    private data class ModbusFunctionReference(
        val hexCode: String,
        val standardMeaning: String,
        val simulatorFunctionLabel: String,
    )

    private data class ModbusRtuFrameSegment(
        val fieldName: String,
        val bytes: ByteArray,
        val ownership: String,
        val description: String,
    )

    private data class ModbusRtuFrameExample(
        val requestFrameHex: String,
        val responseFrameHex: String,
        val requestPayloadHex: String,
        val responsePayloadHex: String,
        val requestSegments: List<ModbusRtuFrameSegment>,
        val note: String,
    )

    private fun ModbusServiceModel.usesStringRegisters(): Boolean =
        operations.any { operation ->
            operation.parameters.any { parameter -> parameter.valueKind == ModbusValueKind.STRING } ||
                operation.returnType.kind == ModbusReturnKind.STRING ||
                operation.returnType.properties.any { property -> property.valueKind == ModbusValueKind.STRING }
        }

    private fun ModbusReturnTypeModel.stringCharCapacity(): Int = (registerWidth * 2) + 1

    private fun ModbusOperationModel.functionReference(): ModbusFunctionReference =
        when (functionCodeName) {
            "READ_COILS" -> ModbusFunctionReference(hexCode = "0x01", standardMeaning = "读取线圈", simulatorFunctionLabel = "01 Read Coils")
            "READ_DISCRETE_INPUTS" ->
                ModbusFunctionReference(
                    hexCode = "0x02",
                    standardMeaning = "读取离散输入",
                    simulatorFunctionLabel = "02 Read Discrete Inputs",
                )

            "READ_HOLDING_REGISTERS" ->
                ModbusFunctionReference(
                    hexCode = "0x03",
                    standardMeaning = "读取保持寄存器",
                    simulatorFunctionLabel = "03 Read Holding Registers",
                )

            "READ_INPUT_REGISTERS" ->
                ModbusFunctionReference(
                    hexCode = "0x04",
                    standardMeaning = "读取输入寄存器",
                    simulatorFunctionLabel = "04 Read Input Registers",
                )

            "WRITE_SINGLE_COIL" ->
                ModbusFunctionReference(
                    hexCode = "0x05",
                    standardMeaning = "写单个线圈",
                    simulatorFunctionLabel = "05 Write Single Coil",
                )

            "WRITE_SINGLE_REGISTER" ->
                ModbusFunctionReference(
                    hexCode = "0x06",
                    standardMeaning = "写单个寄存器",
                    simulatorFunctionLabel = "06 Write Single Register",
                )

            "WRITE_MULTIPLE_COILS" ->
                ModbusFunctionReference(
                    hexCode = "0x0F",
                    standardMeaning = "写多个线圈",
                    simulatorFunctionLabel = "15 Write Multiple Coils",
                )

            "WRITE_MULTIPLE_REGISTERS" ->
                ModbusFunctionReference(
                    hexCode = "0x10",
                    standardMeaning = "写多个寄存器",
                    simulatorFunctionLabel = "16 Write Multiple Registers",
                )

            else -> error("未知的 Modbus 功能码：$functionCodeName")
        }

    private fun renderSimulatorConnectionMarkdownRows(
        transport: ModbusTransportKind,
        transportDefaults: ModbusTransportDefaults,
    ): List<List<String>> =
        when (transport) {
            ModbusTransportKind.RTU ->
                listOf(
                    listOf("连接模式", "选择 `RTU`。"),
                    listOf("串口", "填写真实串口或仿真串口，默认示例是 `${transportDefaults.rtu.portPath}`。"),
                    listOf("从站地址", "默认 `${transportDefaults.rtu.unitId}`；若现场改过 `Unit ID`，这里同步改成现场值。"),
                    listOf("波特率", "默认 `${transportDefaults.rtu.baudRate}`。"),
                    listOf("数据位", "默认 `${transportDefaults.rtu.dataBits}`。"),
                    listOf("停止位", "默认 `${transportDefaults.rtu.stopBits}`。"),
                    listOf("校验", "默认 `${transportDefaults.rtu.parity}`。"),
                    listOf("超时", "建议先填 `${transportDefaults.rtu.timeoutMs} ms`。"),
                )

            ModbusTransportKind.TCP ->
                listOf(
                    listOf("连接模式", "选择 `TCP`。"),
                    listOf("主机地址", "默认 `${transportDefaults.tcp.host}`，联调时改成设备 IP。"),
                    listOf("端口", "默认 `${transportDefaults.tcp.port}`。"),
                    listOf("从站地址", "默认 `${transportDefaults.tcp.unitId}`；若现场网关改过 `Unit ID`，这里同步改成现场值。"),
                    listOf("超时", "建议先填 `${transportDefaults.tcp.timeoutMs} ms`。"),
                )
        }

    private fun ModbusOperationModel.renderSimulatorRequestRows(transport: ModbusTransportKind): List<List<String>> =
        buildList {
            val functionReference = functionReference()
            add(listOf("主站功能选择", "`${functionReference.simulatorFunctionLabel}`"))
            add(listOf("标准码值", "`${functionReference.hexCode}`"))
            add(listOf("数据区", "`${addressSpace.simulatorAreaName()}`"))
            add(listOf("起始地址", "`${address}`"))
            add(listOf("数量", "`${quantity}`"))
            add(listOf("绝对地址区间", "`${address}..${address + registerSpan - 1}`"))
            add(listOf("测试重点", simulatorVerificationHint()))
            if (transport == ModbusTransportKind.RTU) {
                val frameExample = rtuFrameExample()
                add(listOf("原始请求帧", "`SEND >>>>>>>>>> ${frameExample.requestFrameHex}`"))
                add(listOf("原始响应帧", "`RESPONSE>>>>>>>>>> ${frameExample.responseFrameHex}`"))
                add(listOf("报文说明", frameExample.note))
            }
        }

    private fun ModbusOperationModel.renderStringSimulatorNotes(): List<String> =
        parameters.mapNotNull { parameter -> parameter.renderStringSimulatorNote(address) }

    private fun ModbusParameterModel.renderStringSimulatorNote(baseAddress: Int): String? {
        if (valueKind != ModbusValueKind.STRING) {
            return null
        }
        val startAddress = baseAddress + registerOffset
        val endAddress = startAddress + registerWidth - 1
        return "`$name` 使用 `${codecName}`，绝对寄存器区间 `${startAddress}..${endAddress}`，每个寄存器承载 2 个字节；字符串不足部分补 `0x00`。"
    }

    private fun ModbusPropertyModel.renderStringSimulatorNote(baseAddress: Int): String? {
        if (valueKind != ModbusValueKind.STRING || field == null) {
            return null
        }
        val startAddress = baseAddress + field.registerOffset
        val endAddress = startAddress + field.registerWidth - 1
        return "`$name` 使用 `${field.codecName}`，绝对寄存器区间 `${startAddress}..${endAddress}`，每个寄存器承载 2 个字节；字符串不足部分补 `0x00`。"
    }

    private fun ModbusReturnTypeModel.renderScalarStringSimulatorNote(baseAddress: Int): String? {
        if (kind != ModbusReturnKind.STRING) {
            return null
        }
        val endAddress = baseAddress + registerWidth - 1
        return "返回值使用 `${codecName}`，绝对寄存器区间 `${baseAddress}..${endAddress}`，每个寄存器承载 2 个字节；字符串不足部分补 `0x00`。"
    }

    private fun ModbusOperationModel.simulatorVerificationHint(): String =
        when {
            isReadOperation && usesCoilBits ->
                "返回 `quantity=${quantity}` 个线圈位；按下面字段表逐个核对布尔状态。"
            isReadOperation ->
                "返回 `quantity=${quantity}` 个寄存器；按下面字段表核对数值和字符串内容。"
            returnType.kind == ModbusReturnKind.COMMAND_RESULT ->
                "主站应收到成功响应；若要确认业务效果，再补一次读操作核对状态。"
            else ->
                "按参数表发送写请求，并核对设备是否按预期更新。"
        }

    private fun ModbusAddressSpace.simulatorAreaName(): String =
        when (this) {
            ModbusAddressSpace.COIL_READ,
            ModbusAddressSpace.COIL_WRITE -> "Coils"
            ModbusAddressSpace.DISCRETE_INPUT -> "Discrete Inputs"
            ModbusAddressSpace.INPUT_REGISTER -> "Input Registers"
            ModbusAddressSpace.HOLDING_REGISTER_READ,
            ModbusAddressSpace.HOLDING_REGISTER_WRITE -> "Holding Registers"
        }

    private fun ModbusOperationModel.rtuFrameExample(unitId: Int = 1): ModbusRtuFrameExample {
        val functionCode = functionReference().hexCode.removePrefix("0x").toInt(16)
        val requestSegments =
            mutableListOf(
                ModbusRtuFrameSegment(
                    fieldName = "从站地址",
                    bytes = byteArrayOf(unitId.toByte()),
                    ownership = "协议头",
                    description = "默认 `Unit ID=$unitId`。",
                ),
                ModbusRtuFrameSegment(
                    fieldName = "功能码",
                    bytes = byteArrayOf(functionCode.toByte()),
                    ownership = "协议头",
                    description = "${functionReference().standardMeaning}，标准码值 `${functionReference().hexCode}`。",
                ),
            )
        val payloadSegments =
            when (functionCodeName) {
                "READ_COILS",
                "READ_DISCRETE_INPUTS",
                "READ_HOLDING_REGISTERS",
                "READ_INPUT_REGISTERS" ->
                    listOf(
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 起始地址",
                            bytes = byteArrayOf((address shr 8).toByte(), address.toByte()),
                            ownership = "payload",
                            description = "运行时 `address=$address` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 数量",
                            bytes = byteArrayOf((quantity shr 8).toByte(), quantity.toByte()),
                            ownership = "payload",
                            description = "运行时 `quantity=$quantity` 的大端表示。",
                        ),
                    )

                "WRITE_SINGLE_COIL" ->
                    listOf(
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 线圈地址",
                            bytes = byteArrayOf((address shr 8).toByte(), address.toByte()),
                            ownership = "payload",
                            description = "运行时 `address=$address` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 线圈写入值",
                            bytes = byteArrayOf(0xFF.toByte(), 0x00.toByte()),
                            ownership = "payload",
                            description = "示例里把目标线圈写成 `true`，标准编码是 `FF 00`。",
                        ),
                    )

                "WRITE_SINGLE_REGISTER" ->
                    listOf(
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 寄存器地址",
                            bytes = byteArrayOf((address shr 8).toByte(), address.toByte()),
                            ownership = "payload",
                            description = "运行时 `address=$address` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 寄存器写入值",
                            bytes = byteArrayOf(0x00.toByte(), 0x01.toByte()),
                            ownership = "payload",
                            description = "示例里把目标寄存器写成 `0x0001`。",
                        ),
                    )

                "WRITE_MULTIPLE_COILS" -> {
                    val byteCount = ((quantity + 7) / 8).coerceAtLeast(1)
                    listOf(
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 起始地址",
                            bytes = byteArrayOf((address shr 8).toByte(), address.toByte()),
                            ownership = "payload",
                            description = "运行时 `address=$address` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 数量",
                            bytes = byteArrayOf((quantity shr 8).toByte(), quantity.toByte()),
                            ownership = "payload",
                            description = "运行时 `quantity=$quantity` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 字节数",
                            bytes = byteArrayOf(byteCount.toByte()),
                            ownership = "payload",
                            description = "线圈值区共 `${byteCount}` byte。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 线圈值区",
                            bytes = ByteArray(byteCount),
                            ownership = "payload",
                            description = "示例数据全部按 `0` 填充；每一位对应一个线圈值。",
                        ),
                    )
                }

                "WRITE_MULTIPLE_REGISTERS" -> {
                    val byteCount = quantity * 2
                    listOf(
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 起始地址",
                            bytes = byteArrayOf((address shr 8).toByte(), address.toByte()),
                            ownership = "payload",
                            description = "运行时 `address=$address` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 数量",
                            bytes = byteArrayOf((quantity shr 8).toByte(), quantity.toByte()),
                            ownership = "payload",
                            description = "运行时 `quantity=$quantity` 的大端表示。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 字节数",
                            bytes = byteArrayOf(byteCount.toByte()),
                            ownership = "payload",
                            description = "寄存器值区共 `${byteCount}` byte。",
                        ),
                        ModbusRtuFrameSegment(
                            fieldName = "payload: 寄存器值区",
                            bytes = ByteArray(byteCount),
                            ownership = "payload",
                            description = "示例数据全部按 `0x00` 填充；每两个字节对应一个寄存器。",
                        ),
                    )
                }

                else -> error("未知的 Modbus 功能码：$functionCodeName")
            }
        requestSegments += payloadSegments
        val requestPayloadBytes = payloadSegments.fold(ByteArray(0)) { acc, segment -> acc + segment.bytes }
        val requestBody = byteArrayOf(unitId.toByte(), functionCode.toByte()) + requestPayloadBytes
        val requestFrame = requestBody.appendModbusCrc()
        requestSegments +=
            ModbusRtuFrameSegment(
                fieldName = "CRC16",
                bytes = requestFrame.takeLast(2).toByteArray(),
                ownership = "协议尾",
                description = "Modbus RTU CRC16 校验码。",
            )

        val responseBody =
            when (functionCodeName) {
                "READ_COILS",
                "READ_DISCRETE_INPUTS" -> {
                    val byteCount = ((quantity + 7) / 8).coerceAtLeast(1)
                    byteArrayOf(unitId.toByte(), functionCode.toByte(), byteCount.toByte()) + ByteArray(byteCount)
                }

                "READ_HOLDING_REGISTERS",
                "READ_INPUT_REGISTERS" -> {
                    val byteCount = quantity * 2
                    byteArrayOf(unitId.toByte(), functionCode.toByte(), byteCount.toByte()) + ByteArray(byteCount)
                }

                "WRITE_SINGLE_COIL",
                "WRITE_SINGLE_REGISTER" -> requestBody

                "WRITE_MULTIPLE_COILS",
                "WRITE_MULTIPLE_REGISTERS" ->
                    byteArrayOf(
                        unitId.toByte(),
                        functionCode.toByte(),
                        (address shr 8).toByte(),
                        address.toByte(),
                        (quantity shr 8).toByte(),
                        quantity.toByte(),
                    )

                else -> error("未知的 Modbus 功能码：$functionCodeName")
            }
        val responseFrame = responseBody.appendModbusCrc()
        val responsePayloadBytes = responseBody.copyOfRange(2, responseBody.size)

        val note =
            when {
                isReadOperation && usesCoilBits -> "示例响应里数据区全部按 `0` 演示；实际联调时每一位对应一个线圈状态。"
                isReadOperation -> "示例响应里寄存器数据全部按 `0x0000` 演示；实际联调时按下面字段表解码。"
                functionCodeName == "WRITE_SINGLE_COIL" -> "示例请求里把目标线圈写成 `true`，响应帧会原样回显。"
                functionCodeName == "WRITE_SINGLE_REGISTER" -> "示例请求里把目标寄存器写成 `0x0001`，响应帧会原样回显。"
                else -> "示例请求里写入数据区全部按 `0` 演示，响应帧返回起始地址和数量。"
            }

        return ModbusRtuFrameExample(
            requestFrameHex = requestFrame.toHexStringWithSpaces(),
            responseFrameHex = responseFrame.toHexStringWithSpaces(),
            requestPayloadHex = requestPayloadBytes.toHexStringWithSpaces(),
            responsePayloadHex = responsePayloadBytes.toHexStringWithSpaces(),
            requestSegments = requestSegments,
            note = note,
        )
    }

    private fun ByteArray.appendModbusCrc(): ByteArray {
        var crc = 0xFFFF
        for (byte in this) {
            crc = crc xor (byte.toInt() and 0xFF)
            repeat(8) {
                crc =
                    if ((crc and 0x0001) != 0) {
                        (crc ushr 1) xor 0xA001
                    } else {
                        crc ushr 1
                    }
            }
        }
        return this + byteArrayOf((crc and 0xFF).toByte(), ((crc ushr 8) and 0xFF).toByte())
    }

    private fun ByteArray.toHexStringWithSpaces(): String =
        joinToString(separator = " ") { byte -> "%02X".format(byte.toInt() and 0xFF) }

    private fun renderTransposedFrameSegmentsMarkdownTable(segments: List<ModbusRtuFrameSegment>): String =
        renderMarkdownTable(
            headers = listOf("项目") + segments.map { segment -> segment.fieldName },
            rows =
                listOf(
                    listOf("长度") + segments.map { segment -> "`${segment.bytes.size} byte`" },
                    listOf("示例") + segments.map { segment -> "`${segment.bytes.toHexStringWithSpaces()}`" },
                    listOf("归属") + segments.map { segment -> segment.ownership },
                    listOf("说明") + segments.map { segment -> segment.description },
                ),
        )

    private fun renderTransportDefaultsMarkdownRows(
        transport: ModbusTransportKind,
        transportDefaults: ModbusTransportDefaults,
    ): List<List<String>> =
        when (transport) {
            ModbusTransportKind.RTU ->
                listOf(
                    listOf("Port Path", "`${transportDefaults.rtu.portPath}`"),
                    listOf("Unit ID", "`${transportDefaults.rtu.unitId}`"),
                    listOf("Baud Rate", "`${transportDefaults.rtu.baudRate}`"),
                    listOf("Data Bits", "`${transportDefaults.rtu.dataBits}`"),
                    listOf("Stop Bits", "`${transportDefaults.rtu.stopBits}`"),
                    listOf("Parity", "`${transportDefaults.rtu.parity}`"),
                    listOf("Timeout Ms", "`${transportDefaults.rtu.timeoutMs}`"),
                    listOf("Retries", "`${transportDefaults.rtu.retries}`"),
                )

            ModbusTransportKind.TCP ->
                listOf(
                    listOf("Host", "`${transportDefaults.tcp.host}`"),
                    listOf("Port", "`${transportDefaults.tcp.port}`"),
                    listOf("Unit ID", "`${transportDefaults.tcp.unitId}`"),
                    listOf("Timeout Ms", "`${transportDefaults.tcp.timeoutMs}`"),
                    listOf("Retries", "`${transportDefaults.tcp.retries}`"),
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

    private fun ModbusServiceModel.ktorfitApiInterfaceName(): String =
        interfaceSimpleName + transport.transportId.replaceFirstChar(Char::uppercase) + "Api"

    private fun ModbusServiceModel.ktorfitApiPropertyName(): String =
        interfaceSimpleName.replaceFirstChar(Char::lowercase) +
            transport.transportId.replaceFirstChar(Char::uppercase) +
            "Api"

    private fun ModbusServiceModel.httpRoutePath(
        operationId: String,
    ): String = "${basePath}/${transport.transportId}/${serviceId}/$operationId"

    private fun ModbusServiceModel.springRoutePath(
        operationId: String,
        commonBasePath: String?,
    ): String =
        if (commonBasePath == null) {
            httpRoutePath(operationId)
        } else {
            "/$serviceId/$operationId"
        }

    private fun ModbusServiceModel.springRouteFunctionName(
        transport: ModbusTransportKind,
        methodName: String,
    ): String = buildString {
        append("generated")
        append(transport.transportId.replaceFirstChar(Char::uppercase))
        append(interfaceSimpleName.replaceFirstChar(Char::uppercase))
        append(methodName.replaceFirstChar(Char::uppercase))
        append("Route")
    }

    private fun renderTransportRequestSupport(transport: ModbusTransportKind): String =
        when (transport) {
            ModbusTransportKind.RTU ->
                """
internal interface GeneratedModbusRtuRequestConfig {
    val portPath: String?
    val unitId: Int?
    val baudRate: Int?
    val dataBits: Int?
    val stopBits: Int?
    val parity: ModbusSerialParity?
    val timeoutMs: Long?
    val retries: Int?
}

internal fun GeneratedModbusRtuRequestConfig.toEndpointConfig(defaultConfig: ModbusRtuEndpointConfig): ModbusRtuEndpointConfig =
    DefaultModbusRtuEndpointConfig(
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
internal interface GeneratedModbusTcpRequestConfig {
    val host: String?
    val port: Int?
    val unitId: Int?
    val timeoutMs: Long?
    val retries: Int?
}

internal fun GeneratedModbusTcpRequestConfig.toEndpointConfig(defaultConfig: ModbusTcpEndpointConfig): ModbusTcpEndpointConfig =
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

    private fun escapeKotlinString(text: String): String =
        text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

    private fun escapeComment(text: String): String = text.replace("*/", "* /")

    private fun escapeCComment(text: String): String = text.replace("*/", "* /")

    private fun String.renderParityEnumLiteral(): String =
        when (trim().uppercase()) {
            "NONE" -> "ModbusSerialParity.NONE"
            "EVEN" -> "ModbusSerialParity.EVEN"
            "ODD" -> "ModbusSerialParity.ODD"
            else -> "ModbusSerialParity.NONE"
        }

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

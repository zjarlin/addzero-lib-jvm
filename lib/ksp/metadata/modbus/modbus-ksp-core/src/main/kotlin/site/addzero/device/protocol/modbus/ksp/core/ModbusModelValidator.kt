package site.addzero.device.protocol.modbus.ksp.core

/**
 * Modbus IR 校验器。
 */
object ModbusModelValidator {
    fun validate(services: List<ModbusServiceModel>): List<String> {
        val errors = mutableListOf<String>()
        val routeIndex = mutableMapOf<String, String>()

        services.forEach { service ->
            if (service.serviceId.isBlank()) {
                errors += "服务 ${service.interfaceQualifiedName} 的 serviceId 不能为空。"
            }
            val operationIds = mutableSetOf<String>()
            service.operations.forEach { operation ->
                if (!operationIds.add(operation.operationId)) {
                    errors += "服务 ${service.interfaceQualifiedName} 存在重复的 operationId：${operation.operationId}。"
                }
                val route = "${service.basePath}/${service.transport.transportId}/${service.serviceId}/${operation.operationId}"
                val previous = routeIndex.putIfAbsent(route, service.interfaceQualifiedName)
                if (previous != null && previous != service.interfaceQualifiedName) {
                    errors += "路由冲突：$route 同时来自 $previous 和 ${service.interfaceQualifiedName}。"
                }
                validateParameters(service, operation, errors)
                validateReturnType(service, operation, errors)
            }
            service.workflows.forEach { workflow ->
                if (!operationIds.add(workflow.workflowId)) {
                    errors += "服务 ${service.interfaceQualifiedName} 存在重复的 workflowId：${workflow.workflowId}。"
                }
                val route = "${service.basePath}/${service.transport.transportId}/${service.serviceId}/${workflow.workflowId}"
                val previous = routeIndex.putIfAbsent(route, service.interfaceQualifiedName)
                if (previous != null && previous != service.interfaceQualifiedName) {
                    errors += "路由冲突：$route 同时来自 $previous 和 ${service.interfaceQualifiedName}。"
                }
                validateWorkflow(service, workflow, errors)
            }
        }

        validateAddressConflicts(services, errors)

        return errors
    }

    private fun validateWorkflow(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        errors: MutableList<String>,
    ) {
        when (workflow.kind) {
            ModbusWorkflowKind.FLASH_FIRMWARE -> validateFlashWorkflow(service, workflow, errors)
        }
    }

    private fun validateFlashWorkflow(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        errors: MutableList<String>,
    ) {
        val startOperation = service.operationByMethodName(workflow.startMethodName)
        val chunkOperation = service.operationByMethodName(workflow.chunkMethodName)
        val commitOperation = service.operationByMethodName(workflow.commitMethodName)
        val resetOperation =
            workflow.resetMethodName
                ?.let { methodName -> service.operationByMethodName(methodName) }

        if (startOperation == null) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 缺少必需的低层方法：${workflow.startMethodName}。"
        }
        if (chunkOperation == null) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 缺少必需的低层方法：${workflow.chunkMethodName}。"
        }
        if (commitOperation == null) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 缺少必需的低层方法：${workflow.commitMethodName}。"
        }
        if (startOperation == null || chunkOperation == null || commitOperation == null) {
            return
        }

        if (workflow.returnType.kind != ModbusReturnKind.DTO) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 必须返回 FlashResult DTO。"
        }
        val propertyNames = workflow.returnType.properties.mapTo(linkedSetOf(), ModbusPropertyModel::name)
        listOf("accepted", "summary", "totalBytes", "totalChunks", "resetIssued").forEach { requiredName ->
            if (requiredName !in propertyNames) {
                errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 FlashResult 缺少字段：$requiredName。"
            }
        }

        validateFlashStartOperation(service, workflow, startOperation, errors)
        validateFlashChunkOperation(service, workflow, chunkOperation, errors)
        validateFlashCommitOperation(service, workflow, commitOperation, errors)
        if (resetOperation != null) {
            validateFlashResetOperation(service, workflow, resetOperation, errors)
        }
    }

    private fun validateFlashStartOperation(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        if (operation.functionCodeName != "WRITE_MULTIPLE_REGISTERS") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须使用 WRITE_MULTIPLE_REGISTERS。"
        }
        if (operation.returnType.kind != ModbusReturnKind.COMMAND_RESULT) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须返回 ModbusCommandResult。"
        }
        val totalBytes = operation.parameters.find { parameter -> parameter.name == "totalBytes" }
        if (totalBytes == null || totalBytes.codecName != "U32_BE") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须包含 totalBytes: U32_BE。"
        }
        val crc32 = operation.parameters.find { parameter -> parameter.name == "crc32" }
        if (crc32 != null && crc32.codecName != "U32_BE") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 如果声明 crc32，必须使用 U32_BE。"
        }
        if (operation.parameters.any { parameter -> parameter.name !in setOf("totalBytes", "crc32") }) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 目前只支持 totalBytes 和可选 crc32 两个参数。"
        }
    }

    private fun validateFlashChunkOperation(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        if (operation.functionCodeName != "WRITE_MULTIPLE_REGISTERS") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须使用 WRITE_MULTIPLE_REGISTERS。"
        }
        if (operation.returnType.kind != ModbusReturnKind.COMMAND_RESULT) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须返回 ModbusCommandResult。"
        }
        val sequence = operation.parameters.find { parameter -> parameter.name == "sequence" }
        val usedBytes = operation.parameters.find { parameter -> parameter.name == "usedBytes" }
        if (sequence == null || sequence.codecName != "U16") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须包含 sequence: U16。"
        }
        if (usedBytes == null || usedBytes.codecName != "U16") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须包含 usedBytes: U16。"
        }
        val payloadParameters =
            operation.parameters
                .filter { parameter -> parameter.name != "sequence" && parameter.name != "usedBytes" }
                .sortedBy(ModbusParameterModel::registerOffset)
        if (payloadParameters.isEmpty()) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 至少要包含一个 payload 字段。"
        }
        if (payloadParameters.any { parameter -> parameter.codecName !in setOf("U16", "U32_BE") }) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} payload 字段目前只支持 U16 / U32_BE。"
        }
    }

    private fun validateFlashCommitOperation(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        if (operation.functionCodeName !in setOf("WRITE_SINGLE_REGISTER", "WRITE_MULTIPLE_REGISTERS")) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须使用寄存器写功能码。"
        }
        if (operation.returnType.kind != ModbusReturnKind.COMMAND_RESULT) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须返回 ModbusCommandResult。"
        }
        val totalChunks = operation.parameters.find { parameter -> parameter.name == "totalChunks" }
        if (totalChunks == null || totalChunks.codecName != "U16") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须包含 totalChunks: U16。"
        }
    }

    private fun validateFlashResetOperation(
        service: ModbusServiceModel,
        workflow: ModbusWorkflowModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        if (operation.functionCodeName != "WRITE_SINGLE_COIL") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须使用 WRITE_SINGLE_COIL。"
        }
        if (operation.returnType.kind != ModbusReturnKind.COMMAND_RESULT) {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须返回 ModbusCommandResult。"
        }
        val trigger = operation.parameters.find { parameter -> parameter.name == "trigger" }
        if (trigger == null || trigger.codecName != "BOOL_COIL") {
            errors += "工作流 ${service.interfaceQualifiedName}.${workflow.methodName} 的 ${operation.methodName} 必须包含 trigger: BOOL_COIL。"
        }
    }

    private fun validateAddressConflicts(
        services: List<ModbusServiceModel>,
        errors: MutableList<String>,
    ) {
        ModbusAddressSpace.entries.forEach { addressSpace ->
            val operations =
                services.flatMap { service ->
                    service.operations.map { operation -> service to operation }
                }
                    .filter { (_, operation) -> operation.addressSpace == addressSpace }
                    .sortedBy { (_, operation) -> operation.address }

            operations.zipWithNext().forEach { (left, right) ->
                val (leftService, leftOperation) = left
                val (rightService, rightOperation) = right
                val leftEndExclusive = leftOperation.address + leftOperation.registerSpan
                if (leftEndExclusive > rightOperation.address) {
                    errors +=
                        "地址冲突：${leftService.interfaceQualifiedName}.${leftOperation.methodName} " +
                            "与 ${rightService.interfaceQualifiedName}.${rightOperation.methodName} " +
                            "在 $addressSpace 空间发生重叠，请为其中一个操作显式指定 address。"
                }
            }
        }
    }

    private fun validateParameters(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        val orders = mutableSetOf<Int>()
        val occupied = mutableSetOf<String>()
        operation.parameters.forEach { parameter ->
            if (!orders.add(parameter.order)) {
                errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 存在重复的参数顺序：${parameter.order}。"
            }
            validateCodecShape(
                service = service,
                operation = operation,
                subjectName = parameter.name,
                codecName = parameter.codecName,
                valueKind = parameter.valueKind,
                bitOffset = parameter.bitOffset,
                errors = errors,
            )
            validateCodecUsageInFunction(service, operation, parameter.name, parameter.codecName, errors)
            parameter.locationKeys(operation).forEach { key ->
                if (!occupied.add(key)) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的参数映射冲突：${parameter.name} -> $key。"
                }
            }
        }

        val totalWidth = operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
        val coilWidth = operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + 1 } ?: 0
        when (operation.functionCodeName) {
            "READ_COILS",
            "READ_DISCRETE_INPUTS",
            "READ_INPUT_REGISTERS",
            "READ_HOLDING_REGISTERS" -> {
                if (operation.parameters.isNotEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 是读功能码，不应声明请求参数。"
                }
            }

            "WRITE_SINGLE_COIL" -> {
                if (operation.parameters.size != 1 || operation.parameters.singleOrNull()?.valueKind != ModbusValueKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时只能有一个 Boolean 参数。"
                }
                if (operation.parameters.singleOrNull()?.codecName != "BOOL_COIL") {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时必须使用 BOOL_COIL。"
                }
                if (operation.quantity != 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时 quantity 必须等于 1。"
                }
            }

            "WRITE_MULTIPLE_COILS" -> {
                if (operation.parameters.isEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_MULTIPLE_COILS 时至少需要一个 Boolean 参数。"
                }
                if (operation.parameters.any { parameter -> parameter.codecName != "BOOL_COIL" || parameter.valueKind != ModbusValueKind.BOOLEAN }) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_MULTIPLE_COILS 时所有参数都必须是 BOOL_COIL Boolean。"
                }
                if (operation.quantity in 0 until coilWidth) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于线圈编码宽度 $coilWidth。"
                }
            }

            "WRITE_SINGLE_REGISTER" -> {
                if (totalWidth > 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_REGISTER 时参数宽度不能超过 1 个寄存器。"
                }
                if (operation.quantity != 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_REGISTER 时 quantity 必须等于 1。"
                }
            }

            "WRITE_MULTIPLE_REGISTERS" -> {
                if (operation.quantity in 0 until totalWidth) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于参数编码宽度 $totalWidth。"
                }
            }
        }
    }

    private fun validateReturnType(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        if (operation.isReadOperation) {
            validateReadReturnType(service, operation, errors)
            return
        }

        when (operation.returnType.kind) {
            ModbusReturnKind.UNIT,
            ModbusReturnKind.COMMAND_RESULT -> Unit

            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT,
            ModbusReturnKind.STRING,
            ModbusReturnKind.DTO ->
                errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 是写功能码，只允许返回 Unit 或 ModbusCommandResult。"
        }
    }

    private fun validateReadReturnType(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        when (operation.returnType.kind) {
            ModbusReturnKind.UNIT,
            ModbusReturnKind.COMMAND_RESULT -> {
                errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 是读功能码，不能返回 ${operation.returnType.kind.name}。"
            }

            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT,
            ModbusReturnKind.STRING -> {
                if (operation.quantity < 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的标量返回至少需要 1 个数据单元。"
                }
                if (operation.usesCoilBits && operation.returnType.kind != ModbusReturnKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 读取 coil/discrete 时只能返回 Boolean 或仅包含 BOOL_COIL 字段的 DTO。"
                }
                val valueKind =
                    when (operation.returnType.kind) {
                        ModbusReturnKind.BOOLEAN -> ModbusValueKind.BOOLEAN
                        ModbusReturnKind.INT -> ModbusValueKind.INT
                        ModbusReturnKind.STRING -> ModbusValueKind.STRING
                        else -> null
                    }
                if (valueKind != null) {
                    validateCodecShape(
                        service = service,
                        operation = operation,
                        subjectName = "return",
                        codecName = operation.returnType.codecName,
                        valueKind = valueKind,
                        bitOffset = 0,
                        errors = errors,
                    )
                    validateCodecUsageInFunction(service, operation, "return", operation.returnType.codecName, errors)
                }
                if (operation.quantity < operation.returnType.registerWidth) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于标量返回编码宽度 ${operation.returnType.registerWidth}。"
                }
            }

            ModbusReturnKind.DTO -> {
                if (operation.returnType.properties.isEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 DTO 返回不能为空对象。"
                    return
                }
                val occupied = mutableSetOf<String>()
                operation.returnType.properties.forEach { property ->
                    val field = property.field
                    if (field == null) {
                        errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的返回 DTO 字段 ${property.name} 缺少 @ModbusField。"
                        return@forEach
                    }
                    validateCodecShape(
                        service = service,
                        operation = operation,
                        subjectName = property.name,
                        codecName = field.codecName,
                        valueKind = property.valueKind,
                        bitOffset = field.bitOffset,
                        errors = errors,
                    )
                    validateCodecUsageInFunction(service, operation, property.name, field.codecName, errors)
                    property.locationKeys(operation).forEach { key ->
                        if (!occupied.add(key)) {
                            errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的返回字段映射冲突：${property.name} -> $key。"
                        }
                    }
                    if (operation.usesCoilBits && (field.codecName != "BOOL_COIL" || property.valueKind != ModbusValueKind.BOOLEAN)) {
                        errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 读取 coil/discrete 时 DTO 字段 ${property.name} 必须使用 BOOL_COIL Boolean。"
                    }
                }
                val totalWidth =
                    operation.returnType.properties
                        .mapNotNull { property ->
                            val field = property.field ?: return@mapNotNull null
                            if (operation.usesCoilBits) {
                                field.registerOffset + 1
                            } else {
                                field.registerOffset + field.registerWidth
                            }
                        }.maxOrNull()
                        ?: 0
                if (operation.quantity in 0 until totalWidth) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于返回 DTO 编码宽度 $totalWidth。"
                }
            }
        }
    }

    private fun validateCodecShape(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        subjectName: String,
        codecName: String,
        valueKind: ModbusValueKind,
        bitOffset: Int,
        errors: MutableList<String>,
    ) {
        when (codecName) {
            "BOOL_COIL",
            "BIT_FLAG" -> {
                if (valueKind != ModbusValueKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 $codecName 时必须是 Boolean。"
                }
                if (codecName == "BIT_FLAG" && bitOffset !in 0..15) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 BIT_FLAG 时 bitOffset 必须在 0..15。"
                }
            }

            "U16",
            "U32_BE" -> {
                if (valueKind != ModbusValueKind.INT) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 $codecName 时必须是 Int。"
                }
            }

            "STRING_ASCII",
            "STRING_UTF8" -> {
                if (valueKind != ModbusValueKind.STRING) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 $codecName 时必须是 String。"
                }
                if (bitOffset !in setOf(-1, 0)) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 $codecName 时 bitOffset 必须为 0。"
                }
            }

            else -> errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用了未知 codec：$codecName。"
        }
    }

    private fun validateCodecUsageInFunction(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        subjectName: String,
        codecName: String,
        errors: MutableList<String>,
    ) {
        val expectsCoilCodec = operation.usesCoilBits
        if (expectsCoilCodec && codecName != "BOOL_COIL") {
            errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 位于 coil/discrete 功能码下，只能使用 BOOL_COIL。"
        }
        if (!expectsCoilCodec && codecName == "BOOL_COIL") {
            errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 位于寄存器功能码下，不能使用 BOOL_COIL。"
        }
    }

    private fun ModbusParameterModel.locationKeys(operation: ModbusOperationModel): Set<String> =
        when {
            operation.usesCoilBits -> setOf("c${registerOffset}")
            codecName == "BIT_FLAG" -> setOf("r${registerOffset}:b${bitOffset}")
            else -> (registerOffset until registerOffset + registerWidth).mapTo(linkedSetOf()) { register -> "r$register" }
        }

    private fun ModbusPropertyModel.locationKeys(operation: ModbusOperationModel): Set<String> {
        val field = field ?: return emptySet()
        return when {
            operation.usesCoilBits -> setOf("c${field.registerOffset}")
            field.codecName == "BIT_FLAG" -> setOf("r${field.registerOffset}:b${field.bitOffset}")
            else -> (field.registerOffset until field.registerOffset + field.registerWidth).mapTo(linkedSetOf()) { register -> "r$register" }
        }
    }

    private fun ModbusServiceModel.operationByMethodName(methodName: String): ModbusOperationModel? =
        operations.firstOrNull { operation -> operation.methodName == methodName }
}

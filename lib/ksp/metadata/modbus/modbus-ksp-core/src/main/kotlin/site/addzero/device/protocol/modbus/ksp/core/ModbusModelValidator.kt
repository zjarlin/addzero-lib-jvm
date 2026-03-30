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
        }

        validateAddressConflicts(services, errors)

        return errors
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
            validateCodecCompatibility(
                service = service,
                operation = operation,
                subjectName = parameter.name,
                codecName = parameter.codecName,
                valueKind = parameter.valueKind,
                bitOffset = parameter.bitOffset,
                errors = errors,
            )
            parameter.locationKeys().forEach { key ->
                if (!occupied.add(key)) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的参数寄存器映射冲突：${parameter.name} -> $key。"
                }
            }
        }

        val totalSpan = operation.parameterSpan()
        when (operation.functionCodeName) {
            "WRITE_SINGLE_COIL" -> {
                if (operation.parameters.size != 1 || operation.parameters.singleOrNull()?.valueKind != ModbusValueKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时只能有一个 Boolean 参数。"
                }
                if (operation.quantity != 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时 quantity 必须等于 1。"
                }
            }

            "WRITE_MULTIPLE_COILS" -> {
                if (operation.parameters.isEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_MULTIPLE_COILS 时至少需要一个参数。"
                }
                if (operation.parameters.any { parameter -> parameter.valueKind != ModbusValueKind.BOOLEAN || parameter.codecName != "BOOL_COIL" }) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_MULTIPLE_COILS 时参数必须全部为 BOOL_COIL Boolean。"
                }
                if (operation.quantity in 0 until totalSpan) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于线圈写入跨度 $totalSpan。"
                }
            }

            "WRITE_SINGLE_REGISTER" -> {
                if (totalSpan > 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_REGISTER 时参数宽度不能超过 1 个寄存器。"
                }
                if (operation.quantity != 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_REGISTER 时 quantity 必须等于 1。"
                }
            }

            "WRITE_MULTIPLE_REGISTERS" -> {
                if (operation.quantity in 0 until totalSpan) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于参数编码宽度 $totalSpan。"
                }
            }
        }
    }

    private fun validateReturnType(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        when (operation.returnType.kind) {
            ModbusReturnKind.UNIT,
            ModbusReturnKind.COMMAND_RESULT -> Unit

            ModbusReturnKind.BOOLEAN -> {
                if (operation.quantity < 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 Boolean 返回至少需要 1 个数据位。"
                }
            }

            ModbusReturnKind.INT -> {
                if (operation.quantity < 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 Int 返回至少需要 1 个寄存器。"
                }
            }

            ModbusReturnKind.DTO -> {
                val occupied = mutableSetOf<String>()
                if (operation.returnType.properties.isEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 DTO 返回不能为空对象。"
                }
                operation.returnType.properties.forEach { property ->
                    val field = property.field
                    if (field == null) {
                        errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的返回 DTO 字段 ${property.name} 缺少 @ModbusField。"
                    } else {
                        validateCodecCompatibility(
                            service = service,
                            operation = operation,
                            subjectName = property.name,
                            codecName = field.codecName,
                            valueKind = property.valueKind,
                            bitOffset = field.bitOffset,
                            errors = errors,
                        )
                        property.locationKeys().forEach { key ->
                            if (!occupied.add(key)) {
                                errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的返回字段寄存器映射冲突：${property.name} -> $key。"
                            }
                        }
                    }
                }
                val totalSpan = operation.returnSpan()
                if (operation.quantity in 0 until totalSpan) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于返回编码跨度 $totalSpan。"
                }
            }
        }
    }

    private fun validateCodecCompatibility(
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

            else -> errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用了未知 codec：$codecName。"
        }
    }

    private fun ModbusOperationModel.parameterSpan(): Int =
        when (functionCodeName) {
            "WRITE_SINGLE_COIL" -> 1
            "WRITE_MULTIPLE_COILS" -> parameters.maxOfOrNull { parameter -> parameter.registerOffset + 1 } ?: 0
            else -> parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
        }

    private fun ModbusOperationModel.returnSpan(): Int =
        when {
            returnType.kind != ModbusReturnKind.DTO -> quantity.coerceAtLeast(0)
            usesCoilBits ->
                returnType.properties
                    .mapNotNull { property -> property.field?.let { field -> field.registerOffset + 1 } }
                    .maxOrNull()
                    ?: 0

            else ->
                returnType.properties
                    .mapNotNull { property -> property.field?.let { field -> field.registerOffset + field.registerWidth } }
                    .maxOrNull()
                    ?: 0
        }

    private fun ModbusParameterModel.locationKeys(): Set<String> =
        when (codecName) {
            "BIT_FLAG" -> setOf("r${registerOffset}:b${bitOffset}")
            "BOOL_COIL" -> setOf("c$registerOffset")
            else -> (registerOffset until registerOffset + registerWidth).mapTo(linkedSetOf()) { register -> "r$register" }
        }

    private fun ModbusPropertyModel.locationKeys(): Set<String> {
        val field = field ?: return emptySet()
        return when (field.codecName) {
            "BIT_FLAG" -> setOf("r${field.registerOffset}:b${field.bitOffset}")
            "BOOL_COIL" -> setOf("c${field.registerOffset}")
            else -> (field.registerOffset until field.registerOffset + field.registerWidth).mapTo(linkedSetOf()) { register -> "r$register" }
        }
    }
}

package site.addzero.device.protocol.modbus.ksp.core

/**
 * Modbus 契约默认值解析器。
 *
 * 把方法名、数量、地址这些可推导元数据统一收敛到这里，
 * 避免调用侧继续手填重复信息。
 */
internal object ModbusContractDefaultsResolver {
    fun defaultServiceId(interfaceName: String): String =
        defaultOperationId(interfaceName.removeSuffix("Api"))

    fun defaultOperationId(methodName: String): String =
        methodName
            .replace(Regex("([a-z0-9])([A-Z])"), "$1-$2")
            .replace(Regex("[^A-Za-z0-9-]+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .lowercase()

    fun resolveFunctionCodeName(
        explicitFunctionCodeName: String,
        parameters: List<ModbusParameterModel>,
        returnType: ModbusReturnTypeModel,
    ): String {
        if (explicitFunctionCodeName.isNotBlank() && explicitFunctionCodeName != "AUTO") {
            return explicitFunctionCodeName
        }
        if (parameters.isEmpty()) {
            return when (returnType.kind) {
                ModbusReturnKind.BOOLEAN,
                ModbusReturnKind.INT,
                ModbusReturnKind.STRING,
                ModbusReturnKind.DTO -> "READ_INPUT_REGISTERS"
                ModbusReturnKind.UNIT,
                ModbusReturnKind.COMMAND_RESULT -> error("无参数且返回 ${returnType.kind.name} 的操作无法自动推导 functionCode，请显式声明。")
            }
        }

        val totalWidth = parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
        val allBooleans = parameters.isNotEmpty() && parameters.all { parameter -> parameter.valueKind == ModbusValueKind.BOOLEAN }
        if (parameters.size == 1) {
            val parameter = parameters.single()
            if (parameter.valueKind == ModbusValueKind.BOOLEAN) {
                return "WRITE_SINGLE_COIL"
            }
            if (totalWidth <= 1) {
                return "WRITE_SINGLE_REGISTER"
            }
        }
        if (allBooleans) {
            return "WRITE_MULTIPLE_COILS"
        }

        return "WRITE_MULTIPLE_REGISTERS"
    }

    fun resolveOperations(
        serviceId: String,
        operations: List<ModbusOperationModel>,
    ): List<ModbusOperationModel> =
        operations
            .resolveOperationIds()
            .resolveQuantities()
            .resolveAddresses(serviceId)

    private fun List<ModbusOperationModel>.resolveOperationIds(): List<ModbusOperationModel> =
        map { operation ->
            if (operation.operationId.isNotBlank()) {
                operation
            } else {
                operation.copy(operationId = defaultOperationId(operation.methodName))
            }
        }

    private fun List<ModbusOperationModel>.resolveQuantities(): List<ModbusOperationModel> =
        map { operation ->
            if (operation.quantity >= 0) {
                operation
            } else {
                operation.copy(quantity = inferQuantity(operation))
            }
        }

    private fun List<ModbusOperationModel>.resolveAddresses(serviceId: String): List<ModbusOperationModel> =
        map { operation ->
            if (operation.address >= 0) {
                operation
            } else {
                operation.copy(address = stableAddressOf(serviceId, operation))
            }
        }

    private fun inferQuantity(operation: ModbusOperationModel): Int =
        when (operation.functionCodeName) {
            "WRITE_SINGLE_COIL",
            "WRITE_SINGLE_REGISTER" -> 1
            "WRITE_MULTIPLE_COILS" ->
                operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + 1 } ?: 0
            "WRITE_MULTIPLE_REGISTERS" ->
                operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
            "READ_COILS",
            "READ_DISCRETE_INPUTS",
            "READ_INPUT_REGISTERS",
            "READ_HOLDING_REGISTERS" -> inferredReturnWidth(operation.returnType)
            else -> error("暂不支持的功能码：${operation.functionCodeName}")
        }

    private fun inferredReturnWidth(returnType: ModbusReturnTypeModel): Int =
        when (returnType.kind) {
            ModbusReturnKind.UNIT,
            ModbusReturnKind.COMMAND_RESULT -> 0
            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT,
            ModbusReturnKind.STRING -> returnType.registerWidth
            ModbusReturnKind.DTO ->
                returnType.properties.maxOfOrNull { property ->
                    property.field?.let { field -> field.registerOffset + field.registerWidth } ?: 0
                } ?: 0
        }

    private fun defaultBaseAddress(addressSpace: ModbusAddressSpace): Int =
        when (addressSpace) {
            ModbusAddressSpace.COIL_READ,
            ModbusAddressSpace.DISCRETE_INPUT,
            ModbusAddressSpace.COIL_WRITE -> 0
            ModbusAddressSpace.INPUT_REGISTER -> 0
            ModbusAddressSpace.HOLDING_REGISTER_READ -> 320
            ModbusAddressSpace.HOLDING_REGISTER_WRITE -> 512
        }

    private fun addressSlotSize(addressSpace: ModbusAddressSpace): Int =
        when (addressSpace) {
            ModbusAddressSpace.COIL_READ,
            ModbusAddressSpace.DISCRETE_INPUT,
            ModbusAddressSpace.COIL_WRITE -> 16
            ModbusAddressSpace.INPUT_REGISTER,
            ModbusAddressSpace.HOLDING_REGISTER_READ -> 16
            ModbusAddressSpace.HOLDING_REGISTER_WRITE -> 1
        }

    private fun stableAddressOf(
        serviceId: String,
        operation: ModbusOperationModel,
    ): Int {
        val slotSize = addressSlotSize(operation.addressSpace)
        val baseAddress = defaultBaseAddress(operation.addressSpace)
        val requiredSlots = ((operation.registerSpan + slotSize - 1) / slotSize).coerceAtLeast(1)
        val availableSlots = ((MAX_MODBUS_ADDRESS - baseAddress + 1) / slotSize) - requiredSlots + 1
        require(availableSlots > 0) {
            "操作 ${serviceId}.${operation.operationId.ifBlank { operation.methodName }} 的寄存器跨度 ${operation.registerSpan} 超出可分配地址空间。"
        }
        val slotIndex = stableHash(operationStableKey(serviceId, operation)).mod(availableSlots)
        return baseAddress + (slotIndex * slotSize)
    }

    private fun operationStableKey(
        serviceId: String,
        operation: ModbusOperationModel,
    ): String =
        buildString {
            append(serviceId)
            append('#')
            append(operation.methodName)
            append('#')
            append(operation.operationId)
            append('#')
            append(operation.functionCodeName)
            append("#params[")
            operation.parameters
                .sortedBy(ModbusParameterModel::order)
                .forEach { parameter ->
                    append(parameter.name)
                    append(':')
                    append(parameter.qualifiedType)
                    append(':')
                    append(parameter.codecName)
                    append(':')
                    append(parameter.registerOffset)
                    append(':')
                    append(parameter.registerWidth)
                    append(';')
                }
            append("]#return[")
            append(operation.returnType.kind.name)
            append(':')
            append(operation.returnType.qualifiedName)
            append(';')
            operation.returnType.properties.forEach { property ->
                append(property.name)
                append(':')
                append(property.qualifiedType)
                append(':')
                append(property.field?.codecName.orEmpty())
                append(':')
                append(property.field?.registerOffset ?: -1)
                append(':')
                append(property.field?.registerWidth ?: 0)
                append(';')
            }
            append(']')
        }

    private fun stableHash(value: String): Int {
        var hash = 0x811C9DC5.toInt()
        value.forEach { char ->
            hash = hash xor char.code
            hash *= 0x01000193
        }
        return hash and Int.MAX_VALUE
    }

    private const val MAX_MODBUS_ADDRESS = 0xFFFF
}

package site.addzero.device.protocol.modbus.ksp.core

internal fun List<ModbusParameterModel>.withSequentialOffsets(): List<ModbusParameterModel> {
    var cursor = 0
    return map { parameter ->
        val resolvedOffset = if (parameter.registerOffset >= 0) parameter.registerOffset else cursor
        cursor = maxOf(cursor, resolvedOffset + parameter.registerWidth)
        parameter.copy(
            registerOffset = resolvedOffset,
            bitOffset = if (parameter.bitOffset >= 0) parameter.bitOffset else 0,
        )
    }
}

internal fun List<ModbusPropertyModel>.withSequentialFieldOffsets(): List<ModbusPropertyModel> {
    var cursor = 0
    return map { property ->
        val field = property.field ?: return@map property
        val resolvedOffset = if (field.registerOffset >= 0) field.registerOffset else cursor
        cursor = maxOf(cursor, resolvedOffset + field.registerWidth)
        property.copy(
            field = field.copy(
                registerOffset = resolvedOffset,
                bitOffset = if (field.bitOffset >= 0) field.bitOffset else 0,
            ),
        )
    }
}

internal fun registerWidth(
    codecName: String,
    length: Int = 1,
): Int =
    when (codecName) {
        "U32_BE" -> 2 * length
        else -> length
    }

internal fun List<ModbusParameterModel>.resolveAutoCodecNames(functionCodeName: String): List<ModbusParameterModel> =
    map { parameter ->
        if (parameter.codecName != "AUTO") {
            parameter
        } else {
            parameter.copy(codecName = inferCodecName(parameter.valueKind, functionCodeName))
        }
    }

internal fun ModbusReturnTypeModel.resolveAutoCodecNames(functionCodeName: String): ModbusReturnTypeModel =
    if (kind == ModbusReturnKind.DTO) {
        copy(
            properties =
                properties.map { property ->
                    val field = property.field
                    if (field == null || field.codecName != "AUTO") {
                        property
                    } else {
                        property.copy(
                            field = field.copy(codecName = inferCodecName(property.valueKind, functionCodeName)),
                        )
                    }
                },
        )
    } else if (codecName == "AUTO" && valueKind != null) {
        val resolvedCodecName = inferCodecName(valueKind, functionCodeName)
        copy(
            codecName = resolvedCodecName,
            registerWidth = registerWidth(resolvedCodecName, length),
        )
    } else {
        copy(registerWidth = registerWidth(codecName, length))
    }

internal fun inferCodecName(
    valueKind: ModbusValueKind,
    functionCodeName: String,
): String =
    when (valueKind) {
        ModbusValueKind.BOOLEAN ->
            if (functionCodeName in setOf("READ_COILS", "READ_DISCRETE_INPUTS", "WRITE_SINGLE_COIL", "WRITE_MULTIPLE_COILS")) {
                "BOOL_COIL"
            } else {
                "BIT_FLAG"
            }

        ModbusValueKind.INT -> "U16"
        ModbusValueKind.STRING -> "STRING_UTF8"
    }

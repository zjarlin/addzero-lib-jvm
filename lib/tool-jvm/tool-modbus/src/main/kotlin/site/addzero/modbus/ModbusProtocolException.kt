package site.addzero.modbus

import com.ghgande.j2mod.modbus.ModbusSlaveException

/**
 * 表示从站按 Modbus 协议返回了异常响应。
 *
 * 这和“端口断开”“连接失败”“超时”这类传输层异常不同，
 * 它说明链路是通的，但从站明确返回了异常码。
 */
class ModbusProtocolException(
    message: String,
    val functionCode: Int?,
    val exceptionCode: Int,
    val exceptionName: String,
    cause: Throwable? = null,
) : ModbusToolException(message, cause)

internal fun Throwable.toModbusToolException(
    message: String,
    functionCode: Int? = null,
): ModbusToolException {
    val protocolFault = findModbusProtocolFault()
    if (protocolFault != null) {
        val resolvedFunctionCode = protocolFault.functionCode ?: functionCode
        return ModbusProtocolException(
            message =
                buildProtocolExceptionMessage(
                    baseMessage = message,
                    functionCode = resolvedFunctionCode,
                    exceptionCode = protocolFault.exceptionCode,
                    exceptionName = protocolFault.exceptionName,
                ),
            functionCode = resolvedFunctionCode,
            exceptionCode = protocolFault.exceptionCode,
            exceptionName = protocolFault.exceptionName,
            cause = this,
        )
    }
    return if (this is ModbusToolException) {
        this
    } else {
        ModbusToolException(message, this)
    }
}

private data class ModbusProtocolFault(
    val functionCode: Int?,
    val exceptionCode: Int,
    val exceptionName: String,
)

private fun Throwable.findModbusProtocolFault(): ModbusProtocolFault? {
    generateSequence(this) { current -> current.cause }.forEach { current ->
        when (current) {
            is ModbusProtocolException ->
                return ModbusProtocolFault(
                    functionCode = current.functionCode,
                    exceptionCode = current.exceptionCode,
                    exceptionName = current.exceptionName,
                )

            is ModbusSlaveException ->
                return ModbusProtocolFault(
                    functionCode = null,
                    exceptionCode = current.type,
                    exceptionName = ModbusSlaveException.getMessage(current.type),
                )
        }
    }
    return null
}

private fun buildProtocolExceptionMessage(
    baseMessage: String,
    functionCode: Int?,
    exceptionCode: Int,
    exceptionName: String,
): String =
    buildString {
        append(baseMessage)
        append("；从站返回 Modbus 异常 ")
        append(exceptionName)
        append(" (code=")
        append(exceptionCode)
        functionCode?.let { value ->
            append(", function=0x")
            append(value.toString(16).uppercase().padStart(2, '0'))
        }
        append(')')
    }

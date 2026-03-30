package site.addzero.modbus

/**
 * Modbus 工具层统一异常。
 */
open class ModbusToolException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

package site.addzero.modbus

/**
 * Modbus 工具层统一异常。
 *
 * 这样调用方不必感知 j2mod 在不同 API 下抛出的各种底层异常类型，
 * 只需要在边界上统一捕获这一类异常即可。
 */
open class ModbusToolException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

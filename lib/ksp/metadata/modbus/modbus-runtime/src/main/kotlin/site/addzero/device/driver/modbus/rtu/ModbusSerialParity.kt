package site.addzero.device.driver.modbus.rtu

/**
 * 运行时统一使用的串口校验位抽象。
 *
 * 这里不直接暴露底层串口库类型，避免上层配置模型被工具实现细节污染。
 */
enum class ModbusSerialParity {
    NONE,
    EVEN,
    ODD,
}

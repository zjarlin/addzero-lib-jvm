package site.addzero.device.driver.modbus.rtu

/**
 * 暴露默认 RTU 执行器公开入口，供外部模块在不依赖内部构造器的前提下复用 tool-backed 实现。
 */
fun createDefaultModbusRtuExecutor(): ModbusRtuExecutor {
    return J2modModbusRtuExecutor()
}

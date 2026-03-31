package site.addzero.device.driver.modbus.rtu

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Modbus RTU 运行时的 Koin 模块。
 *
 * 统一通过 KCP 注解扫描收集运行时实现，不再暴露手写 DSL 模块。
 */
@Module
@ComponentScan("site.addzero.device.driver.modbus.rtu")
class ModbusRuntimeKoinModule

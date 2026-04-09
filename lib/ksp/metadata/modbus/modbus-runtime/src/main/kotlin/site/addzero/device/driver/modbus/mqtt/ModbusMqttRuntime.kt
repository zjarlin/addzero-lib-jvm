package site.addzero.device.driver.modbus.mqtt

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * 默认先暴露一个可注入的 MQTT 执行器占位实现。
 *
 * 这样 metadata generator 生成出来的 MQTT gateway 至少可以编译、装配、被替换；
 * 真正的 broker 交互实现后续再按现场协议补进来。
 */
@Single
class DefaultModbusMqttExecutor : UnsupportedModbusMqttExecutor()

@Module
@ComponentScan("site.addzero.device.driver.modbus.mqtt")
class ModbusMqttRuntimeKoinModule

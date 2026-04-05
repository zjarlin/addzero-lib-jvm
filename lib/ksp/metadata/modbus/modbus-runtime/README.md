# modbus-runtime

Modbus 运行时骨架模块。

- Maven 坐标：`site.addzero:modbus-runtime`
- 本地路径：`lib/ksp/metadata/modbus/modbus-runtime`
- 作用：
  - 提供 RTU / TCP 端点配置
  - 提供执行器接口与默认实现
  - 提供编解码辅助函数
  - 提供 Modbus 注解与协议模型
  - 提供可直接装入 Koin 的运行时模块

## 运行时包含什么

- `driver/modbus/rtu`
  - `ModbusRtuEndpointConfig`
  - `DefaultModbusRtuEndpointConfig`
  - `ModbusRtuExecutor`
  - `ModbusRtuConfigRegistry`
  - `ModbusRuntimeKoinModule`
- `driver/modbus/tcp`
  - `ModbusTcpEndpointConfig`
  - `ModbusTcpExecutor`
  - `ModbusTcpConfigRegistry`
  - `ModbusTcpRuntimeKoinModule`
- `protocol/modbus`
  - `ModbusCodecSupport`
  - `annotation/ModbusAnnotations`
  - `model/ModbusFunctionCode`
  - `model/ModbusCodec`
  - `model/ModbusCommandResult`

## 怎么在 Koin 里装

```kotlin
install(Koin) {
    withConfiguration<YourKoinApplication>()
}
```

或者在 `@KoinApplication` 根入口里直接挂：

```kotlin
@KoinApplication(
    modules = [
        ModbusRuntimeKoinModule::class,
        ModbusTcpRuntimeKoinModule::class,
    ],
)
class YourKoinApplication
```

## 当前状态

- `driver/modbus/rtu` 已经接入真实 JVM RTU 执行器：
  - `j2mod`
  - `jSerialComm`
- `ModbusRtuEndpointConfig` 现在是接口；
  应用根入口通常只需要向 Koin 提供一份全局默认实现，例如 `DefaultModbusRtuEndpointConfig`。
- `driver/modbus/tcp` 已提供 JVM TCP 执行器。
- `ModbusTcpConfigRegistry` 建议由应用根入口按明确 provider 显式提供，不再依赖 DSL `getAll()` 聚合。

## 典型用法

```kotlin
@Module
class DeviceGatewayKoinModule {
    @Single
    fun rtuEndpointConfig(): ModbusRtuEndpointConfig = DefaultModbusRtuEndpointConfig(
        portPath = "/dev/ttyUSB0",
        unitId = 1,
        baudRate = 9600,
        timeoutMs = 1000,
        retries = 2,
    )

    @Single
    fun tcpConfigRegistry(
        deviceProvider: DeviceGeneratedTcpConfigProvider,
    ): ModbusTcpConfigRegistry = ModbusTcpConfigRegistry(listOf(deviceProvider))
}
```

然后在生成出来的 gateway 里，会读取这份默认端点配置，必要时叠加请求级覆盖项，并调用执行器真正完成：

- `read_input_registers`
- `read_holding_registers`
- `write_single_coil`
- `write_multiple_registers`

## 当前 MVP 默认串口参数

- `unit_id = 1`
- `115200 8N1`
- `timeout = 1000ms`
- `retries = 2`

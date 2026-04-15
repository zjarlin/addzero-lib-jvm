# modbus-runtime

Modbus 注解、编码工具、传输配置模型，以及 RTU/TCP/MQTT 运行时抽象都在这个模块里。

- Maven 坐标：`site.addzero:modbus-runtime`
- 本地路径：`lib/ksp/metadata/modbus/modbus-runtime`

## 谁应该直接依赖它

下面这些场景都应该直接依赖 `modbus-runtime`：

- 业务模块要写 Modbus 契约接口和 DTO。
- 业务模块要编译 `modbus-ksp-rtu`、`modbus-ksp-tcp`、`modbus-ksp-mqtt` 生成出来的 Kotlin 源码。
- 业务模块自己提供 RTU/TCP/MQTT 默认配置。
- 业务模块要直接调用 `ModbusCodecSupport` 做编码解码。

## 它提供什么

### `commonMain`

- 注解
  - `@GenerateModbusRtuServer`
  - `@GenerateModbusTcpServer`
  - `@GenerateModbusMqttServer`
  - `@ModbusOperation`
  - `@ModbusParam`
  - `@ModbusField`
- 协议模型
  - `ModbusFunctionCode`
  - `ModbusCodec`
  - `ModbusCommandResult`
- 编解码工具
  - `ModbusCodecSupport`
- 传输抽象
  - `ModbusRtuEndpointConfig`
  - `ModbusTcpEndpointConfig`
  - `ModbusMqttEndpointConfig`
  - 各 transport 的 `*ConfigProvider`、`*ConfigRegistry`、`*Executor`

### `jvmMain`

- RTU 真实执行器
  - `J2modModbusRtuExecutor`
- TCP 真实执行器
  - `J2modModbusTcpExecutor`
- Koin 运行时模块
  - `ModbusRuntimeKoinModule`
  - `ModbusTcpRuntimeKoinModule`
  - `ModbusMqttRuntimeKoinModule`
- MQTT 默认占位执行器
  - `DefaultModbusMqttExecutor`
  - 这个实现的目标是“先让生成代码能编译和注入”，不是现成的真实 broker 适配器

## 最小用法

### 1. 在契约源码里使用注解

```kotlin
import site.addzero.device.protocol.modbus.annotation.GenerateModbusRtuServer
import site.addzero.device.protocol.modbus.annotation.ModbusField
import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

@GenerateModbusRtuServer
interface DeviceApi {
    @ModbusOperation(
        address = 0,
        functionCode = ModbusFunctionCode.READ_COILS,
    )
    suspend fun getInfo(): DeviceInfo
}

data class DeviceInfo(
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 0)
    val running: Boolean,
)
```

### 2. 在业务层提供 RTU 默认配置

```kotlin
import site.addzero.device.driver.modbus.rtu.DefaultModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigProvider
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig

class DeviceRtuConfigProvider : ModbusRtuConfigProvider {
    override fun defaultConfig(): ModbusRtuEndpointConfig =
        DefaultModbusRtuEndpointConfig(
            portPath = "/dev/ttyUSB0",
            unitId = 1,
            baudRate = 9600,
            timeoutMs = 1_000,
            retries = 2,
        )
}
```

### 3. 在业务层提供 TCP 默认配置

```kotlin
import site.addzero.device.driver.modbus.tcp.ModbusTcpConfigProvider
import site.addzero.device.driver.modbus.tcp.ModbusTcpEndpointConfig

class DeviceTcpConfigProvider : ModbusTcpConfigProvider {
    override val serviceId: String = "device"

    override fun defaultConfig(): ModbusTcpEndpointConfig =
        ModbusTcpEndpointConfig(
            serviceId = serviceId,
            host = "127.0.0.1",
            port = 502,
            unitId = 1,
            timeoutMs = 1_000,
            retries = 2,
        )
}
```

### 4. MQTT 现在要注意什么

`modbus-runtime` 已经提供：

- `ModbusMqttEndpointConfig`
- `ModbusMqttConfigProvider`
- `ModbusMqttConfigRegistry`
- `ModbusMqttExecutor`
- `ModbusMqttRuntimeKoinModule`

但默认注入的 `DefaultModbusMqttExecutor` 只是一个占位实现。

也就是说：

- 你可以先把 `modbus-ksp-mqtt` 生成出来的代码编译通过。
- 真要跑到现场 broker，就得在业务层替换成自己的 MQTT executor。

## 它不负责什么

`modbus-runtime` 不会：

- 自动运行 KSP。
- 自动生成 `GeneratedModbusRtu.kt` / `GeneratedModbusTcp.kt` / `GeneratedModbusMqtt.kt`。
- 自动扫描你的契约接口。

这些事情要交给 `modbus-*-gradle-plugin` 或 `modbus-ksp-*` 处理器模块。

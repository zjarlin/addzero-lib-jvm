# modbus-runtime

Modbus 协议契约与 JVM 执行器运行时。

- Maven 坐标：`site.addzero:modbus-runtime`
- 本地路径：`lib/ksp/metadata/modbus/modbus-runtime`

## 模块分层

- `commonMain`
  - `annotation/ModbusAnnotations`
  - `model/ModbusFunctionCode`
  - `model/ModbusCodec`
  - `model/ModbusCommandResult`
  - `ModbusCodecSupport`
  - RTU/TCP 端点配置与纯抽象接口
- `jvmMain`
  - RTU/TCP 真实执行器
  - `tool-modbus` / `tool-serial` 适配
  - Koin runtime module

这意味着前端 KMP 模块现在可以直接依赖 `site.addzero:modbus-runtime` 来复用：

- `@ModbusField`
- `@ModbusOperation`
- `ModbusCodec`
- `ModbusFunctionCode`
- `ModbusCommandResult`
- 以及纯配置模型

不再需要把这类 DTO 留在 JVM-only source set。

## 前端共享模型示例

```kotlin
data class Device24PowerLights(
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 0)
    val light1: Boolean,
)
```

## JVM 运行时装配示例

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
}
```

如果需要 Koin 扫描模块，继续使用：

- `ModbusRuntimeKoinModule`
- `ModbusTcpRuntimeKoinModule`

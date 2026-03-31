# Modbus Metadata Suite

`modbus` 现在不是单个 KSP renderer，而是一套可扩展的协议元数据 suite。

## Module Split

- `modbus-rtu-gradle-plugin`
  - 项目级 RTU KSP 消费插件，推荐入口 `site.addzero.ksp.modbus-rtu`。
- `modbus-tcp-gradle-plugin`
  - 项目级 TCP KSP 消费插件，推荐入口 `site.addzero.ksp.modbus-tcp`。
- `modbus-runtime`
  - 注解、功能码枚举、RTU/TCP runtime 抽象。
- `modbus-ksp-core`
  - 语义 DTO -> Modbus 的共享 IR、默认值解析、校验、suite facade、SPI。
- `modbus-ksp-kotlin-gateway`
  - 生成 Kotlin 调用端 gateway。
- `modbus-ksp-c-contract`
  - 生成 C service contract / transport dispatch / adapter。
- `modbus-ksp-markdown`
  - 生成 markdown 协议文档。
- `modbus-ksp-rtu`
  - RTU processor 入口，组合上面三个生成模块。
- `modbus-ksp-tcp`
  - TCP processor 入口，组合上面三个生成模块。

## Recommended Consumption

默认走项目级 Gradle plugin，不再优先要求业务工程手写 processor 依赖：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

TCP 同理：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

只有在你明确需要直接控制 processor artifact 时，才退回到底层 `ksp(...)` 接法。

如果是跨仓库本地联调，并且消费仓库已经把 `../addzero-lib-jvm` 的相关模块 remap 成 project path，推荐做法是：

1. 在 `addzero-lib-jvm` 里先把 `gradle-ksp-consumer-base` 和 `modbus-*-gradle-plugin` 发布到 `mavenLocal`
2. 消费仓库继续通过 `plugins { id("site.addzero.ksp.modbus-*") }` 使用 typed DSL
3. 让处理器与 runtime 通过 remap 进来的本地 project path 优先解析

不推荐把整个 `addzero-lib-jvm` 塞进消费仓库的 `pluginManagement.includeBuild(...)`。当前这套 Modbus suite 在这种接法下会把配置阶段带到 `modbus-ksp-core` 的缺失 project path 问题。

## SPI Contract

core 通过 `ServiceLoader<ModbusArtifactGenerator>` 聚合输出模块：

- `KOTLIN_GATEWAY`
- `C_SERVICE_CONTRACT`
- `C_TRANSPORT_CONTRACT`
- `MARKDOWN_PROTOCOL`

这层设计的目的不是只服务 Modbus，而是把“语义契约 -> 多协议输出”的边界先定住。后面扩 MQTT 时，可以复用同一套 suite/facade/SPI 思路，再加 `mqtt-*` 输出模块，而不是把所有协议逻辑继续塞回同一个 renderer。

## Semantic Contract Direction

本轮支持的主路径是：

```kotlin
@GenerateModbusRtuServer
interface DeviceService {
    @ModbusOperation(
        operationId = "get-device-info",
        address = 0,
        quantity = 24,
        functionCode = ModbusFunctionCode.READ_COILS
    )
    suspend fun getDeviceInfo(): DeviceInfo24
}

data class DeviceInfo24(
    @ModbusField(codec = "BOOL_COIL", registerOffset = 0) val ch1: Boolean,
    @ModbusField(codec = "BOOL_COIL", registerOffset = 1) val ch2: Boolean,
    // ...
    @ModbusField(codec = "BOOL_COIL", registerOffset = 23) val ch24: Boolean,
)
```

然后：

- Kotlin side 生成 `executor.readCoils(...)` 调用代码
- C side 生成 `*_generated.h/.c`、`*_bridge.h`、`modbus_rtu_dispatch.*`
- Markdown side 生成协议表格文档

## Current Firmware Example

`/Users/zjarlin/IdeaProjects/t` 当前已经接入最小示例：

- `getDeviceInfo(): DeviceInfo24`
- `READ_COILS address=0 quantity=24`
- 业务实现读取 `CH1_OUT..CH24_OUT`
- `freertos.c` 改为走 generated agile adapter

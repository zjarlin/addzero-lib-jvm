# Modbus Metadata Suite

`modbus` 现在不是单个 KSP renderer，而是一套可扩展的协议元数据 suite。

## Module Split

- `modbus-rtu-gradle-plugin`
  - 项目级 RTU KSP 消费插件，推荐入口 `site.addzero.ksp.modbus-rtu`。
- `modbus-tcp-gradle-plugin`
  - 项目级 TCP KSP 消费插件，推荐入口 `site.addzero.ksp.modbus-tcp`。
- `modbus-mqtt-gradle-plugin`
  - 项目级 MQTT KSP 消费插件，推荐入口 `site.addzero.ksp.modbus-mqtt`。
- `modbus-runtime`
  - 注解、功能码枚举、RTU/TCP/MQTT runtime 抽象。
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
- `modbus-ksp-mqtt`
  - MQTT processor 入口，组合上面三个生成模块。

## Recommended Consumption

默认走项目级 Gradle plugin，不再优先要求业务工程手写 processor 依赖：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    transports.set(listOf("rtu"))
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
    transports.set(listOf("tcp"))
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

MQTT 同理：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-mqtt")
}

modbusMqtt {
    transports.set(listOf("mqtt"))
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

只有在你明确需要直接控制 processor artifact 时，才退回到底层 `ksp(...)` 接法。

## Metadata Inputs

`modbus-ksp` 现在不再强绑 `KSClassDeclaration` 作为唯一输入。

processor 会通过 `ServiceLoader<ModbusMetadataProvider>` 收集元数据提供者，V1 内置两个默认实现：

- `interfaces`
  - 从 Kotlin 注解接口抽取契约元数据。
- `database`
  - 通过 JDBC 查询 JSON 元数据，再归一化成同一套 `ModbusServiceModel`。

如果你没有显式配置 `metadataProviders`，所有已发现的 provider 都会参与，但每个 provider 会自行判断是否启用：

- `interfaces` 需要 `contractPackages`
- `database` 需要 `databaseJdbcUrl` + `databaseQuery`

接口抽取模式：

```kotlin
modbusRtu {
    transports.set(listOf("rtu"))
    metadataProviders.set(listOf("interfaces"))
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

数据库模式：

```kotlin
modbusRtu {
    transports.set(listOf("rtu"))
    metadataProviders.set(listOf("database"))
    codegenModes.set(listOf("server"))

    databaseDriverClass.set("org.sqlite.JDBC")
    databaseJdbcUrl.set("jdbc:sqlite:/absolute/path/codegen-context.db")
    databaseQuery.set(
        """
        select payload
        from codegen_context_modbus_contract
        where transport = '${'$'}{transport}'
        """.trimIndent(),
    )
    databaseJsonColumn.set("payload")
}
```

`databaseQuery` 支持两个占位符：

- `${transport}`
  - 例如 `rtu` / `tcp`
- `${transportName}`
  - 例如 `RTU` / `TCP`

数据库 payload 支持三种 JSON 形态：

- 单个 service 对象
- service 数组
- `{ "services": [...] }`

这意味着后续 `codegen-context` 只要把数据库里的一行 JSON 组织成标准 payload，就能桥接到同一条 Modbus KSP 生成链路，不需要再伪造 Kotlin 接口源码。

如果你要同时产出多种传输层目标，统一用 `transports`：

```kotlin
modbusRtu {
    transports.set(listOf("rtu", "tcp"))
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

当前真正已实现的 provider 只有：

- `rtu`
- `tcp`
- `mqtt`

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

元数据输入侧对应的是 `ServiceLoader<ModbusMetadataProvider>`：

- `interfaces`
- `database`

这层设计的目的不是只服务 Modbus，而是把“语义契约 -> 多协议输出”的边界先定住。后面扩 MQTT 时，可以复用同一套 suite/facade/SPI 思路，再加 `mqtt-*` 输出模块，而不是把所有协议逻辑继续塞回同一个 renderer。

## Firmware Project Sync

如果你要把生成的 C 代码直接落到固件工程，再顺手同步 Keil / CubeMX 工程文件，推荐用 typed DSL：

```kotlin
modbusRtu {
    transports.set(listOf("rtu"))
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))

    cOutputProjectDir.set("/Users/zjarlin/IdeaProjects/t")
    bridgeImplPath.set("Core/Src/modbus")
    keilUvprojxPath.set("MDK-ARM/test1.uvprojx")
    keilTargetName.set("test1")
    keilGroupName.set("Core/modbus/rtu")
    mxprojectPath.set(".mxproject")
}
```

字段含义：

- `cOutputProjectDir`
  - 固件工程根目录。
- `bridgeImplPath`
  - 可编辑 bridge 实现根目录。
  - 例如 RTU 会生成到 `Core/Src/modbus/rtu/<service>/<service>_bridge_impl.c`。
- `keilUvprojxPath`
  - 要同步的 `.uvprojx`。
- `keilTargetName`
  - 要更新的 target 名称；留空则匹配第一个 target。
- `keilGroupName`
  - Keil group 前缀；RTU 默认 `Core/modbus/rtu`，TCP 默认 `Core/modbus/tcp`。
- `mxprojectPath`
  - 要同步的 `.mxproject`。

生成后的目录约定：

- 不可编辑 generated 头文件：
  - `Core/Inc/generated/modbus/<transport>/...`
- 不可编辑 generated 源文件：
  - `Core/Src/generated/modbus/<transport>/...`
- 可编辑 bridge 实现：
  - `Core/Src/modbus/<transport>/<service>/<service>_bridge_impl.c`

项目文件同步范围：

- 会改：
  - `.uvprojx`
  - `.mxproject`
- 不会改：
  - `.uvoptx`
  - `.ioc`

原因很直接：

- `.uvoptx` 是开发者本机 UI/调试偏好，不该由 KSP 覆盖。
- `.ioc` 是硬件配置源，不负责维护这批 generated C 文件清单。

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

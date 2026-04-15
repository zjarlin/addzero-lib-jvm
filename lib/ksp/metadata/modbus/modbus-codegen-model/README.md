# modbus-codegen-model

`modbus-codegen-model` 提供纯 Kotlin 的 Modbus 元数据模型与序列化 schema。

- Maven 坐标：`site.addzero:modbus-codegen-model`
- 本地路径：`lib/ksp/metadata/modbus/modbus-codegen-model`

## 什么时候用它

这个模块适合下面这些场景：

- 你要在 KSP 之外表达一份标准化的 Modbus 元数据。
- 你要给 `database` provider 生产 JSON payload。
- 你要写外部工具、脚本、同步服务，和 Modbus 代码生成链共享同一套数据结构。
- 你想在单测里直接构造 `ModbusServiceModel` / `ModbusProtocolSuiteModel`，而不是跑整条 KSP。

## 它包含什么

核心模型包括：

- `ModbusTransportKind`
- `ModbusServiceModel`
- `ModbusOperationModel`
- `ModbusWorkflowModel`
- `ModbusReturnTypeModel`
- `GeneratedArtifact`
- `ModbusProtocolSuiteModel`
- `ModbusArtifactRenderContext`

同时也包含标准 JSON payload 对应的 `@Serializable` 模型：

- `ModbusMetadataEnvelope`
- `ModbusMetadataServicePayload`
- `ModbusMetadataOperationPayload`
- `ModbusMetadataWorkflowPayload`
- 以及一组 `*Payload` 子模型

## 最小用法

如果你只是想在外部工具里组装一份元数据对象，可以直接依赖这个模块，然后构造模型：

```kotlin
import site.addzero.device.protocol.modbus.codegen.model.ModbusDocModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusServiceModel

val transport = ModbusTransportKind.RTU

val service =
    ModbusServiceModel(
        interfacePackage = "site.addzero.device.contract",
        interfaceSimpleName = "DeviceApi",
        interfaceQualifiedName = "site.addzero.device.contract.DeviceApi",
        serviceId = "device",
        summary = "设备接口",
        basePath = "/api/modbus",
        transport = transport,
        doc = ModbusDocModel(summary = "设备接口"),
        operations = emptyList(),
    )
```

通常它会和 [`modbus-codegen-core`](../modbus-codegen-core/README.md) 搭配使用：

- `modbus-codegen-model`
  - 负责“数据长什么样”。
- `modbus-codegen-core`
  - 负责“怎么解析、校验、推导、渲染”。

## 它不负责什么

这个模块刻意不负责：

- KSP 扫描
- ServiceLoader SPI 装配
- Gradle plugin 注入
- 文件落盘

它就是纯模型层。

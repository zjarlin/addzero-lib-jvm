# modbus-codegen-core

`modbus-codegen-core` 是不依赖 KSP 的纯 Kotlin 元数据处理模块。

- Maven 坐标：`site.addzero:modbus-codegen-core`
- 本地路径：`lib/ksp/metadata/modbus/modbus-codegen-core`

## 什么时候用它

下面这些场景适合直接依赖它：

- 你有一份标准 Modbus JSON payload，想把它解析成统一模型。
- 你要在 KSP 之外做默认值推导、地址/数量推导、模型校验。
- 你要根据统一元数据直接生成纯 Kotlin contract。
- 你在写外部工具、预处理链、codegen-context 桥接层。

## 这个模块最有用的 API

- `ModbusMetadataJsonCodec`
  - 把 JSON payload 编解码为 `ModbusServiceModel`
- `ModbusContractDefaultsResolver`
  - 推导默认 `serviceId`、`operationId`、`functionCode`、`quantity`
- `ModbusModelValidator`
  - 校验地址冲突、寄存器跨度、类型与 codec 合法性
- `ModbusKotlinContractGenerator`
  - 根据统一模型直接渲染纯 Kotlin contract

## 最小用法

### 1. 解析标准 JSON payload

```kotlin
import site.addzero.device.protocol.modbus.codegen.ModbusMetadataJsonCodec
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportKind

val services =
    ModbusMetadataJsonCodec.decodeServices(
        payload = jsonPayload,
        transport = ModbusTransportKind.RTU,
        contractPackages = listOf("site.addzero.device.contract"),
    )
```

### 2. 再渲染成纯 Kotlin contract

```kotlin
import site.addzero.device.protocol.modbus.codegen.ModbusKotlinContractGenerationRequest
import site.addzero.device.protocol.modbus.codegen.ModbusKotlinContractGenerator

val artifacts =
    ModbusKotlinContractGenerator.render(
        ModbusKotlinContractGenerationRequest(
            services = services,
        ),
    )
```

`artifacts` 里拿到的是 `GeneratedArtifact`，你可以自己决定写到哪里。

## 和 `modbus-ksp-core` 的区别

- `modbus-codegen-core`
  - 不关心 KSP、Gradle、ServiceLoader
  - 适合外部工具与纯 Kotlin 预处理
- `modbus-ksp-core`
  - 关心 processor 生命周期、metadata provider、artifact generator、project sync SPI
  - 适合 KSP 处理器生态

如果你的问题是“我有一份 Modbus 元数据，怎么先在 JVM 里做解析和 contract 生成”，优先看这个模块。

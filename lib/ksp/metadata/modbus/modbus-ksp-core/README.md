# modbus-ksp-core

`modbus-ksp-core` 是 Modbus KSP 链路的核心 SPI 与共享语义层，不是普通业务模块的直接入口。

- Maven 坐标：`site.addzero:modbus-ksp-core`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-core`

## 谁应该依赖它

只有下面这些场景应该直接依赖 `modbus-ksp-core`：

- 你要实现新的 `ModbusMetadataProvider`。
- 你要实现新的 `ModbusArtifactGenerator`。
- 你要实现新的 `ModbusProjectSyncTool`。
- 你要自己组装一套 Modbus processor，而不是直接复用现成的 RTU/TCP/MQTT 处理器。

普通业务工程不要直接依赖它，直接用：

- `modbus-rtu-gradle-plugin`
- `modbus-tcp-gradle-plugin`
- `modbus-ksp-mqtt`
- `modbus-runtime`

## 它负责什么

### 1. metadata 输入 SPI

- `ModbusMetadataProvider`
- `ModbusMetadataCollector`
- 内置 provider
  - `interfaces`
  - `database`

### 2. artifact 输出 SPI

- `ModbusArtifactGenerator`
- `ModbusArtifactRenderer`
- 已定义的 artifact kind
  - `KOTLIN_CONTRACT`
  - `KOTLIN_GATEWAY`
  - `C_SERVICE_CONTRACT`
  - `C_TRANSPORT_CONTRACT`
  - `MARKDOWN_PROTOCOL`

### 3. 外部工程同步 SPI

- `ModbusProjectSyncTool`
- `ModbusProjectSyncRunner`
- 地址锁支持
  - `ModbusAddressLockFile`
  - `ModbusAddressPlanner`

### 4. 共享选项解析

这里统一定义并解析原始 KSP 参数，例如：

- `addzero.modbus.codegen.mode`
- `addzero.modbus.contractPackages`
- `addzero.modbus.metadata.providers`
- `addzero.modbus.database.*`
- `addzero.modbus.spring.route.outputDir`
- `addzero.modbus.apiClientPackageName`
- `addzero.modbus.apiClientOutputDir`
- `addzero.modbus.address.lock.path`

## 最常见的扩展方式

### 扩一个 metadata provider

```kotlin
class MyMetadataProvider : ModbusMetadataProvider {
    override val providerId: String = "my-provider"

    override fun collect(
        context: ModbusMetadataCollectionContext,
    ): List<CollectedModbusService> {
        return emptyList()
    }
}
```

然后在 `META-INF/services/site.addzero.device.protocol.modbus.ksp.core.ModbusMetadataProvider` 注册实现类。

### 扩一个 artifact generator

```kotlin
class MyArtifactGenerator : ModbusArtifactGenerator {
    override val kind: ModbusArtifactKind = ModbusArtifactKind.MARKDOWN_PROTOCOL

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> {
        return emptyList()
    }
}
```

然后在 `META-INF/services/site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactGenerator` 注册实现类。

### 扩一个外部工程同步工具

```kotlin
class MyProjectSyncTool : ModbusProjectSyncTool {
    override val toolId: String = "my-sync"

    override fun isEnabled(context: ModbusProjectSyncContext): Boolean = true

    override fun sync(context: ModbusProjectSyncContext) {
    }
}
```

然后在 `META-INF/services/site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncTool` 注册实现类。

## 你不应该在这里做什么

- 不要把业务契约接口放进这个模块。
- 不要把 transport 专属模板直接写死在 core 里。
- 不要让业务工程直接依赖这个模块来“碰运气触发生成”。

`modbus-ksp-core` 的职责是“统一语义 + 统一 SPI”，不是“最终对业务暴露的消费入口”。

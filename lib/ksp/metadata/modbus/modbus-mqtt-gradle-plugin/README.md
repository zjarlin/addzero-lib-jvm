# modbus-mqtt-gradle-plugin

项目级 Modbus MQTT KSP 消费插件。

- Plugin id：`site.addzero.ksp.modbus-mqtt`
- Maven 坐标：`site.addzero:modbus-mqtt-gradle-plugin`
- 本地路径：`lib/ksp/metadata/modbus/modbus-mqtt-gradle-plugin`

## 先说定位

这个插件模块在仓库里是可用的，也能提供和 RTU/TCP 类似的 typed DSL 体验。

但要把状态说清楚：

- 仓库级 KSP policy 当前明确列为默认保留 consumer plugin 的是 `modbus-rtu` 和 `modbus-tcp`
- `modbus-mqtt-gradle-plugin` 没有出现在那份默认保留清单里

所以它更适合：

- 仓库内使用
- 受控消费仓库联调
- 已经决定沿用这套 MQTT 插件包装的项目

如果你要新增一个“默认对外推广”的 MQTT 接入方案，先确认发布策略；如果你只是想要最直接可控的底层入口，可以直接用 [`modbus-ksp-mqtt`](../modbus-ksp-mqtt/README.md)。

## 这个插件帮你做了什么

- 自动应用 `com.google.devtools.ksp`
- 自动注入 `site.addzero:modbus-ksp-mqtt`
- 自动补 `site.addzero:modbus-runtime`
- 提供 `modbusMqtt {}` typed DSL

## 最小用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-mqtt")
}

modbusMqtt {
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## 常用 DSL

### 通用项

- `codegenModes`
- `contractPackages`
- `metadataProviders`
- `transports`
- `databaseDriverClass`
- `databaseJdbcUrl`
- `databaseUsername`
- `databasePassword`
- `databaseQuery`
- `databaseJsonColumn`
- `cOutputProjectDir`
- `bridgeImplPath`
- `markdownOutputPath`
- `keilUvprojxPath`
- `keilTargetName`
- `keilGroupName`
- `mxprojectPath`
- `springRouteOutputDir`

### MQTT 默认传输参数

- `mqttBrokerUrl`
- `mqttClientId`
- `mqttRequestTopic`
- `mqttResponseTopic`
- `mqttQos`
- `mqttTimeoutMs`
- `mqttRetries`

同时也保留了 RTU/TCP 默认传输参数 DSL，因为底层生成模板统一使用同一套 transport defaults 模型。

## 什么时候退回原始处理器

如果你需要下面这些底层参数，就直接用 [`modbus-ksp-mqtt`](../modbus-ksp-mqtt/README.md)：

- `addzero.modbus.address.lock.path`
- `addzero.modbus.apiClientPackageName`
- `addzero.modbus.apiClientOutputDir`

## 一个运行时提醒

就算你通过这个插件成功生成了 MQTT 网关代码，`modbus-runtime` 当前默认提供的 MQTT executor 仍然是占位实现。

也就是说，这个插件解决的是“怎么把 MQTT 代码生成出来”，不是“现场 broker 如何真正接通”。

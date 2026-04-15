# modbus-ksp-mqtt

`modbus-ksp-mqtt` 是 Modbus MQTT 的原始 KSP 处理器入口。

- Maven 坐标：`site.addzero:modbus-ksp-mqtt`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-mqtt`

## 什么时候直接用它

如果你要最底层、最可控的 MQTT 生成接法，就直接用它。

典型场景：

- 你要手写原始 `ksp { arg(...) }`
- 你要用 `address.lock.path`、`apiClient*` 这类 typed DSL 没包起来的参数
- 你要在仓库里联调 MQTT 生成链

如果你已经决定在仓库里沿用现成插件包装，也可以看 [`modbus-mqtt-gradle-plugin`](../modbus-mqtt-gradle-plugin/README.md)。

## 最小用法

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-mqtt"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "server,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
    arg("addzero.modbus.transports", "mqtt")
}
```

契约接口应使用：

- `@GenerateModbusMqttServer`
- `@ModbusOperation`
- `@ModbusParam`
- `@ModbusField`

## 模式说明

- `server`
  - 生成 `GeneratedModbusMqtt.kt`
- `contract`
  - 生成 C 合同、dispatch、Markdown 文档
  - 对无源码契约 provider 还会额外生成 Kotlin contract

## MQTT 默认传输参数

原始 KSP 还支持这组 MQTT 默认值：

- `addzero.modbus.mqtt.default.brokerUrl`
- `addzero.modbus.mqtt.default.clientId`
- `addzero.modbus.mqtt.default.requestTopic`
- `addzero.modbus.mqtt.default.responseTopic`
- `addzero.modbus.mqtt.default.qos`
- `addzero.modbus.mqtt.default.timeoutMs`
- `addzero.modbus.mqtt.default.retries`

这些值会进入生成代码的默认配置模型。

## 一个必须说清楚的现实

`modbus-ksp-mqtt` 能生成 MQTT 侧 Kotlin 网关，但 `modbus-runtime` 当前默认提供的 `DefaultModbusMqttExecutor` 只是占位实现。

这意味着：

- 生成链路是通的，代码能生成、能编译、能注入。
- 真正对接现场 broker 时，你还需要在业务层提供自己的 MQTT executor。

## 常见输出

- `build/generated/ksp/main/kotlin/.../GeneratedModbusMqtt.kt`
- `build/generated/ksp/main/resources/generated/modbus/mqtt/*.h`
- `build/generated/ksp/main/resources/generated/modbus/mqtt/*.c`
- `build/generated/ksp/main/resources/generated/modbus/protocols/*.md`

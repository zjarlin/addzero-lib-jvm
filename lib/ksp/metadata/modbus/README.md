# Modbus KSP

`lib/ksp/metadata/modbus` 现在只保留一个对消费方可见的主处理器：`modbus-ksp`。

## 当前结构

- `modbus-ksp`
  - Modbus 主 KSP 处理器。
  - 内部已经合并 metadata SPI、artifact 生成器、Keil/CubeMX 同步器，以及 RTU/TCP/MQTT 多 transport provider。
- `modbus-codegen-core`
  - Kotlin contract 代码生成底层库，给 `modbus-ksp` 内部使用。
- `modbus-runtime`
  - 生成出来的 Kotlin gateway / server 代码所需运行时。
- `example/modbus/modbus-ksp-rtu-smoke`
  - 仓库内冒烟验证模块。

## 消费方式

不再保留 `modbus-rtu-gradle-plugin`、`modbus-tcp-gradle-plugin`、`modbus-mqtt-gradle-plugin` 这类消费插件。

业务侧统一用普通 KSP 依赖：

```kotlin
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("site.addzero:modbus-runtime:VERSION")
    ksp("site.addzero:modbus-ksp:VERSION")
}

ksp {
    arg("addzero.modbus.transports", "rtu")
    arg("addzero.modbus.codegen.mode", "server,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

KMP 消费侧继续按普通原生方式接：

```kotlin
dependencies {
    kspJvm("site.addzero:modbus-ksp:VERSION")
    commonMainImplementation("site.addzero:modbus-runtime:VERSION")
}
```

## 常用参数

- `addzero.modbus.transports`
  - 多值逗号分隔，可选 `rtu`、`tcp`、`mqtt`。
- `addzero.modbus.codegen.mode`
  - 可选 `server`、`contract`，支持多值。
- `addzero.modbus.contractPackages`
  - 需要扫描的 contract 包。
- `addzero.modbus.address.lock.path`
  - 地址锁文件路径。现在同一个文件可以同时承载多 transport。
- `addzero.modbus.rtu.default.*`
  - RTU 默认端口与串口参数。
- `addzero.modbus.tcp.default.*`
  - TCP 默认 host / port / timeout 参数。
- `addzero.modbus.mqtt.default.*`
  - MQTT 默认 broker / topic / qos 参数。

## 消费方 SPI

C 产物落盘和固件工程同步不再通过主处理器公开 KSP 参数传入。

- 外部 C 工程目录、bridge 实现目录、Markdown 输出目录
- Keil `.uvprojx` 目标、group
- CubeMX `.mxproject` 路径

这些都应该由消费方自己在 `ksp(...)` classpath 上提供 `ModbusConsumerCArtifactsProvider` SPI。

## 维护约束

- 不要再新增 transport 级别的独立 KSP 入口模块。
- 不要再给 Modbus 额外包一层消费 Gradle 插件。
- 生成器、同步器、metadata provider 在 `modbus-ksp` 内用分包区分即可，不再继续细拆子模块。

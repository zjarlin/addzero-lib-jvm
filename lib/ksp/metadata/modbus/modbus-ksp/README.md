# modbus-ksp

`modbus-ksp` 是 Modbus 的主 KSP 处理器，也是这个目录里唯一推荐给消费方直接依赖的处理器工件。

## 职责

- 采集 Modbus contract metadata。
- 统一处理 RTU / TCP / MQTT 多 transport 生成。
- 生成 Kotlin contract、Kotlin gateway、C 合同、Markdown 协议文档。
- 按需同步 Keil `.uvprojx` 和 CubeMX `.mxproject`。
- 维护地址锁文件，保证协议地址稳定。

## 依赖方式

```kotlin
dependencies {
    implementation("site.addzero:modbus-runtime:VERSION")
    ksp("site.addzero:modbus-ksp:VERSION")
}
```

如果是 Kotlin Multiplatform，按原生 KSP 配置接 `kspJvm(...)` 或项目内 `project(":lib:ksp:metadata:modbus:modbus-ksp")`。

## 最小参数

```kotlin
ksp {
    arg("addzero.modbus.transports", "tcp")
    arg("addzero.modbus.codegen.mode", "server")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

## 消费方 SPI

`modbus-ksp` 的 Kotlin / metadata 侧选项仍然通过普通 KSP `arg(...)` 传入。

但下面这些强绑定消费方工程布局的 C 集成配置，不再建议直接作为主处理器参数暴露：

- 外部固件工程目录
- bridge 实现目录
- Keil `.uvprojx` / target / group
- CubeMX `.mxproject`

消费方应该额外在 `ksp(...)` classpath 上提供 `ModbusConsumerCArtifactsProvider` SPI，由该 SPI 决定这些落盘与同步目标。

## 设计说明

- 不再拆 `modbus-ksp-rtu` / `modbus-ksp-tcp` / `modbus-ksp-mqtt` 三个独立处理器。
- 不再依赖 Modbus 专用消费 Gradle 插件。
- artifact 生成器和同步工具在同一模块内用分包隔离，而不是继续拆成更多 KSP 子模块。

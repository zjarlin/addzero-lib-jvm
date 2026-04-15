# modbus-ksp-c-contract

`modbus-ksp-c-contract` 是 C 产物生成器模块，它通过 `ServiceLoader` 向 `modbus-ksp-core` 注册：

- `C_SERVICE_CONTRACT`
- `C_TRANSPORT_CONTRACT`

- Maven 坐标：`site.addzero:modbus-ksp-c-contract`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-c-contract`

## 它生成什么

这个模块负责输出 C 侧协议契约相关文件，例如：

- 每个 service 的 `*_generated.h`
- 每个 service 的 `*_generated.c`
- 每个 service 的 `*_bridge.h`
- 每个 service 的 `*_bridge_impl.c`
- transport 级别的 `modbus_<transport>_dispatch.h`
- transport 级别的 `modbus_<transport>_dispatch.c`

RTU 场景下还会生成 transport 适配文件，例如冒烟测试里的：

- `modbus_rtu_agile_slave_adapter.h`
- `modbus_rtu_agile_slave_adapter.c`

## 谁应该直接依赖它

一般不是业务模块。

直接依赖它的应该是：

- `modbus-ksp-rtu`
- `modbus-ksp-tcp`
- `modbus-ksp-mqtt`
- 你自己组装的 Modbus processor 模块

## 最小接法

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-core"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-c-contract"))
}
```

只要它在处理器 classpath 上，`ModbusArtifactRenderer` 就能通过 SPI 找到它。

## 业务层是怎么“用到”它的

业务层通常不会直接依赖这个模块，而是通过更高层入口间接用到它：

- `site.addzero.ksp.modbus-rtu`
- `site.addzero.ksp.modbus-tcp`
- `modbus-ksp-mqtt`

如果再配合：

- `addzero.modbus.c.output.projectDir`
- `addzero.modbus.c.bridgeImpl.path`

这些生成出的 C 文件还会被镜像到外部固件工程。

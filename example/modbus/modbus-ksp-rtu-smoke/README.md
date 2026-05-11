# modbus-ksp-rtu-smoke

`modbus-ksp-rtu-smoke` 是仓库内部的 RTU 冒烟验证模块，用来确认 `modbus-ksp` 在 raw KSP 方式下还能完整跑通。

这个样例不再依赖源码注解扫描，而是模拟真实消费方：

- 业务模块自己额外提供一个挂在 `ksp(...)` 上的 `ModbusMetadataProvider` SPI 实现。
- 该 provider 在处理期把 SQLite 元数据表喂给 `modbus-ksp`。
- 业务模块还会额外提供 `ModbusConsumerCArtifactsProvider` SPI，决定外部 C 工程、Keil、CubeMX 的落盘和同步目标。
- `modbus-ksp` 再基于这份外部 metadata 生成 Kotlin gateway / contract、C 合同和协议文档。

它会真实验证：

- `ksp(project(":lib:ksp:metadata:modbus:modbus-ksp"))` 的接线是否有效。
- `ksp(project(":example:modbus:modbus-ksp-rtu-smoke-provider"))` 这种消费方自带 SPI provider 的方式是否有效。
- RTU transport 的 Kotlin gateway / contract 是否正常生成。
- 外部 C 工程桥接、Keil、CubeMX 同步是否正常落盘。
- 地址锁文件是否稳定。

运行方式：

```bash
./gradlew :example:modbus:modbus-ksp-rtu-smoke:test
```

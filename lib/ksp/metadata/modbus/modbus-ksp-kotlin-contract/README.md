# modbus-ksp-kotlin-contract

Modbus 纯 Kotlin contract 生成模块。

- Maven 坐标：`site.addzero:modbus-ksp-kotlin-contract`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-kotlin-contract`
- 作用：
  - 复用 `modbus-codegen-core` 的纯 Kotlin contract 渲染器
  - 通过 `ModbusArtifactGenerator` SPI 接入 `modbus-ksp-core`
  - 让 `rtu/tcp/mqtt` processor 在 `contract` 模式下为无源码契约的 metadata provider 输出接口与 DTO

这个模块不提供新的 KSP entrypoint，只是 contract artifact 的独立生成器实现。

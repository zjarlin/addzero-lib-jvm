# modbus-ksp-core

Modbus KSP 共享 IR、校验与渲染核心。

- Maven 坐标：`site.addzero:modbus-ksp-core`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-core`
- 作用：
  - 解析 Kotlin 源接口为内部 IR
  - 执行注解约束校验
  - 渲染 Ktor / Koin / C 产物文本

## core 负责什么

- `ModbusSymbolCollector`
  - 把注解接口收敛成统一 IR
- `ModbusModelValidator`
  - 校验 operation id、寄存器重叠、类型与 codec 兼容性
  - `ModbusArtifactRenderer`
  - 输出：
    - `GeneratedModbusRtu.kt`
    - `GeneratedModbusTcp.kt`
    - `modbus_rtu_dispatch.h`
    - `modbus_rtu_dispatch.c`
    - `modbus_tcp_dispatch.h`
    - `modbus_tcp_dispatch.c`
    - `*_generated.h`
    - `*_generated.c`
    - `*_bridge.h`
    - `*_bridge.sample.c`

## 设计约束

- `process` 阶段只收集，`finish` 阶段统一生成。
- 处理器模块不包含任何业务契约接口，只面向 `modbus-runtime` 暴露的协议注解与模型包。
- 对下游 `server` 来说，注解必须是 `BINARY` 可见，否则依赖 jar 上读不到。

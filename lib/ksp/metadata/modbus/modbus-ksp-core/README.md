# modbus-ksp-core

Modbus KSP 共享 IR、校验与渲染核心。

- Maven 坐标：`site.addzero:modbus-ksp-core`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-core`
- 作用：
  - 通过 SPI 收集 Modbus 元数据并归一化为内部 IR
  - 执行注解约束校验
  - 渲染 Ktor / Koin / C 产物文本

## core 负责什么

- `ModbusMetadataProvider`
  - 元数据输入 SPI。
  - 当前内置 `interfaces` 与 `database` 两个实现。
- `ModbusMetadataCollector`
  - 通过 `ServiceLoader` 发现 provider，并按配置选择输入源。
- `ModbusSymbolCollector`
  - `interfaces` provider 的默认 Kotlin 接口抽取器。
- `ModbusDatabaseMetadataProvider`
  - `database` provider 的默认 JDBC + JSON 抽取器。
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

## 元数据输入约定

processor 现在不再要求“必须先存在一个 Kotlin 接口”。

统一入口是 `ModbusServiceModel`，只要 provider 能产出这套模型，后面的校验和多产物渲染都复用同一条链路。

默认 provider：

- `interfaces`
  - 读取 `contractPackages` 下的注解接口。
- `database`
  - 读取 JDBC 查询结果里的 JSON payload。

数据库 provider 需要的选项：

- `addzero.modbus.metadata.providers=database`
- `addzero.modbus.database.driverClass`
- `addzero.modbus.database.jdbcUrl`
- `addzero.modbus.database.username`
- `addzero.modbus.database.password`
- `addzero.modbus.database.query`
- `addzero.modbus.database.jsonColumn`

如果 `addzero.modbus.metadata.providers` 不配置，core 会尝试所有已发现 provider，再由每个 provider 自行决定是否启用。

## 设计约束

- `process` 阶段只收集，`finish` 阶段统一生成。
- 处理器模块不包含任何业务契约接口，只面向 `modbus-runtime` 暴露的协议注解与模型包。
- 对下游 `server` 来说，注解必须是 `BINARY` 可见，否则依赖 jar 上读不到。

# spec-iot

通用 IoT JVM 规范库，抽离了“物模型定义 + 协议采集解码 + 标准化 telemetry 报文 + TDengine SQL 规划”这 4 层共性能力。

- Maven 坐标：`site.addzero:spec-iot`
- 本地路径：`/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/biz/spec-iot`
- 目标平台：JVM / Java 8+

## 行业分层总结

物联网项目里最容易复用的是这几层：

1. 物模型层：设备/产品有哪些属性、属性类型是什么。
2. 协议层：S7、Modbus 等协议怎么读取、写入、解码。
3. telemetry 层：把采集值转成统一报文。
4. 时序存储层：按物模型把属性映射成 TDengine 表结构和查询 SQL。

不适合进这个库的是：

- Spring / MyBatis / RocketMQ / XXL-Job 等框架胶水
- Redis DAO、告警规则、业务副作用
- 产品、设备、点位这些项目内数据库实体

## 核心能力

- `IotThingRef`、`IotPropertySpec`、`TelemetryReport` 等基础模型
- `ServiceLoader` SPI：
  - `IotPropertySpecProvider`
  - `TelemetryTableNamingStrategy`
  - `TdengineTypeMappingProvider`
- TDengine 表结构 diff 与 SQL builder
- S7 批量读取 / 单点写入 / telemetry 解码
- Modbus 批量读取 / 单点写入 / telemetry 解码

## SPI 注册

消费方需要自己提供物模型定义。示例：

```kotlin
class DemoPropertySpecProvider : IotPropertySpecProvider {

    override val name: String = "demo"

    override fun supports(thingRef: IotThingRef): Boolean {
        return thingRef.kind == "product" && thingRef.id == "demo-product"
    }

    override fun getPropertySpecs(thingRef: IotThingRef): List<IotPropertySpec> {
        return listOf(
            IotPropertySpec.builder()
                .identifier("temperature")
                .name("Temperature")
                .valueType(IotValueType.FLOAT32)
                .build(),
            IotPropertySpec.builder()
                .identifier("running")
                .name("Running")
                .valueType(IotValueType.BOOLEAN)
                .build(),
        )
    }
}
```

在 `META-INF/services/site.addzero.biz.spec.iot.spi.IotPropertySpecProvider` 中声明实现类全限定名。

## TDengine SQL Builder

```kotlin
val product = IotThingRef.of("product", "demo-product")
val builder = TdengineTelemetrySqlBuilder()

val createStable = builder.buildCreateStable(product)
val migration = builder.buildSchemaMigration(product, emptyList())
val latestQuery = builder.buildLatestQuery(product, "temperature")
```

## S7 / Modbus 示例

```kotlin
val s7Report = S7TelemetryDecoder().decodeReport(
    IotThingRef.of("product", "demo-product"),
    IotThingRef.of("device", "device-1"),
    LocalDateTime.now(),
    payload,
    bindings,
)

val modbusReport = ModbusTelemetryDecoder().decodeReport(
    IotThingRef.of("product", "demo-product"),
    IotThingRef.of("device", "device-1"),
    LocalDateTime.now(),
    rawValues,
    points,
)
```

## 约束

- 第一版只生成 SQL 与参数模型，不内置 JDBC / MyBatis 执行器。
- 模块源码与测试采用 Kotlin-only，不包含 Java 源文件。
- 默认 TDengine 命名策略会把 `thing kind/id` 规范化后生成表名。
- 物模型、命名与类型映射全部允许消费方通过 `ServiceLoader` 覆盖。
- `S7Client` / `ModbusClient` 采用反射式可选适配：
  如果消费方要启用协议通信，需要自行把 `com.github.xingshuangs:iot-communication`、`com.infiniteautomation:modbus4j` 放进运行时 classpath。

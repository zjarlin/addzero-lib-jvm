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

```java
public final class DemoPropertySpecProvider implements IotPropertySpecProvider {

    @Override
    public String getName() {
        return "demo";
    }

    @Override
    public boolean supports(IotThingRef thingRef) {
        return "product".equals(thingRef.getKind()) && "demo-product".equals(thingRef.getId());
    }

    @Override
    public List<IotPropertySpec> getPropertySpecs(IotThingRef thingRef) {
        return Arrays.asList(
                IotPropertySpec.builder()
                        .identifier("temperature")
                        .name("Temperature")
                        .valueType(IotValueType.FLOAT32)
                        .build(),
                IotPropertySpec.builder()
                        .identifier("running")
                        .name("Running")
                        .valueType(IotValueType.BOOLEAN)
                        .build()
        );
    }
}
```

在 `META-INF/services/site.addzero.biz.spec.iot.spi.IotPropertySpecProvider` 中声明实现类全限定名。

## TDengine SQL Builder

```java
IotThingRef product = IotThingRef.of("product", "demo-product");
TdengineTelemetrySqlBuilder builder = new TdengineTelemetrySqlBuilder();

SqlStatement createStable = builder.buildCreateStable(product);
List<SqlStatement> migration = builder.buildSchemaMigration(product, Collections.emptyList());
SqlStatement latestQuery = builder.buildLatestQuery(product, "temperature");
```

## S7 / Modbus 示例

```java
TelemetryReport s7Report = new S7TelemetryDecoder().decodeReport(
        IotThingRef.of("product", "demo-product"),
        IotThingRef.of("device", "device-1"),
        LocalDateTime.now(),
        payload,
        bindings
);

TelemetryReport modbusReport = new ModbusTelemetryDecoder().decodeReport(
        IotThingRef.of("product", "demo-product"),
        IotThingRef.of("device", "device-1"),
        LocalDateTime.now(),
        rawValues,
        points
);
```

## 约束

- 第一版只生成 SQL 与参数模型，不内置 JDBC / MyBatis 执行器。
- 默认 TDengine 命名策略会把 `thing kind/id` 规范化后生成表名。
- 物模型、命名与类型映射全部允许消费方通过 `ServiceLoader` 覆盖。

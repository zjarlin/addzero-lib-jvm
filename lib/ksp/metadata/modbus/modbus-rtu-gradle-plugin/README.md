# modbus-rtu-gradle-plugin

项目级 Modbus RTU KSP 消费插件，也是当前最推荐的 RTU 接入入口。

- Plugin id：`site.addzero.ksp.modbus-rtu`
- Maven 坐标：`site.addzero:modbus-rtu-gradle-plugin`
- 本地路径：`lib/ksp/metadata/modbus/modbus-rtu-gradle-plugin`

## 这个插件帮你做了什么

- 自动应用 `com.google.devtools.ksp`
- 自动注入 `site.addzero:modbus-ksp-rtu`
- 自动补 `site.addzero:modbus-runtime`
- 提供 `modbusRtu {}` typed DSL，把配置转成底层 `ksp.arg(...)`

这就是为什么它比业务模块直接手写 `ksp(project(...))` 更适合作为默认入口。

## 最小用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## 常用 DSL

### 生成模式与输入来源

- `codegenModes`
  - 映射到 `addzero.modbus.codegen.mode`
  - 常用值：`server`、`contract`
- `contractPackages`
  - 映射到 `addzero.modbus.contractPackages`
- `metadataProviders`
  - 映射到 `addzero.modbus.metadata.providers`
  - 常用值：`interfaces`、`database`
- `transports`
  - 映射到 `addzero.modbus.transports`
  - 对 RTU 插件来说，正常保持默认 `rtu` 即可

### 数据库 provider

- `databaseDriverClass`
- `databaseJdbcUrl`
- `databaseUsername`
- `databasePassword`
- `databaseQuery`
- `databaseJsonColumn`

### 外部固件工程联动

- `cOutputProjectDir`
- `bridgeImplPath`
- `markdownOutputPath`
- `keilUvprojxPath`
- `keilTargetName`
- `keilGroupName`
- `mxprojectPath`

### Spring 风格源码

- `springRouteOutputDir`

### RTU/TCP 默认传输参数

RTU 插件还会把这些默认值下发给生成代码：

- `rtuPortPath`
- `rtuUnitId`
- `rtuBaudRate`
- `rtuDataBits`
- `rtuStopBits`
- `rtuParity`
- `rtuTimeoutMs`
- `rtuRetries`
- `tcpHost`
- `tcpPort`
- `tcpUnitId`
- `tcpTimeoutMs`
- `tcpRetries`

其中 TCP 这组默认值存在的原因，是同一套模板里会用到 transport 默认参数模型，不代表 RTU 插件会顺带生成 TCP。

## interfaces provider 示例

```kotlin
modbusRtu {
    metadataProviders.set(listOf("interfaces"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## database provider 示例

```kotlin
modbusRtu {
    metadataProviders.set(listOf("database"))
    databaseDriverClass.set("org.sqlite.JDBC")
    databaseJdbcUrl.set("jdbc:sqlite:/absolute/path/codegen-context.db")
    databaseQuery.set("select payload from codegen_context_modbus_contract where transport = '${'$'}{transport}'")
    databaseJsonColumn.set("payload")
}
```

## 什么时候不要用这个插件

下面这些场景，请直接退回 [`modbus-ksp-rtu`](../modbus-ksp-rtu/README.md)：

- 你需要 `addzero.modbus.address.lock.path`
- 你需要 `addzero.modbus.apiClientPackageName`
- 你需要 `addzero.modbus.apiClientOutputDir`
- 你明确要完全掌控原始 `ksp` wiring

也就是说，这个插件是“推荐入口”，但不是“所有底层选项都暴露完了”的万能入口。

## 跨仓库本地联调

如果消费仓库是另一个仓库，并且通过 project remap 方式指到本地 `addzero-lib-jvm`，推荐做法仍然是保留插件 DSL，然后先把插件发布到 `mavenLocal`：

```bash
cd /Users/zjarlin/IdeaProjects/addzero-lib-jvm
./gradlew \
  :lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base:publishToMavenLocal \
  :lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin:publishToMavenLocal
```

这样做的好处是：

- 业务仓库继续保持稳定的 `plugins {}` / typed DSL。
- `modbus-runtime` 这类 companion 依赖继续由插件统一注入。
- 不必把整个 `addzero-lib-jvm` 通过 `pluginManagement.includeBuild(...)` 暴露给消费仓库。

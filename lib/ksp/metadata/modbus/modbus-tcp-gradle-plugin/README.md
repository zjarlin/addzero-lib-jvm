# modbus-tcp-gradle-plugin

项目级 Modbus TCP KSP 消费插件，也是当前最推荐的 TCP 接入入口。

- Plugin id：`site.addzero.ksp.modbus-tcp`
- Maven 坐标：`site.addzero:modbus-tcp-gradle-plugin`
- 本地路径：`lib/ksp/metadata/modbus/modbus-tcp-gradle-plugin`

## 这个插件帮你做了什么

- 自动应用 `com.google.devtools.ksp`
- 自动注入 `site.addzero:modbus-ksp-tcp`
- 自动补 `site.addzero:modbus-runtime`
- 提供 `modbusTcp {}` typed DSL

## 最小用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## 常用 DSL

### 模式与输入

- `codegenModes`
  - 映射到 `addzero.modbus.codegen.mode`
- `contractPackages`
  - 映射到 `addzero.modbus.contractPackages`
- `metadataProviders`
  - 映射到 `addzero.modbus.metadata.providers`
- `transports`
  - 映射到 `addzero.modbus.transports`
  - TCP 插件通常保持默认 `tcp`

### database provider

- `databaseDriverClass`
- `databaseJdbcUrl`
- `databaseUsername`
- `databasePassword`
- `databaseQuery`
- `databaseJsonColumn`

### 外部固件工程镜像

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

## interfaces provider 示例

```kotlin
modbusTcp {
    metadataProviders.set(listOf("interfaces"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## database provider 示例

```kotlin
modbusTcp {
    metadataProviders.set(listOf("database"))
    databaseDriverClass.set("org.sqlite.JDBC")
    databaseJdbcUrl.set("jdbc:sqlite:/absolute/path/codegen-context.db")
    databaseQuery.set("select payload from codegen_context_modbus_contract where transport = '${'$'}{transport}'")
    databaseJsonColumn.set("payload")
}
```

## 什么时候退回原始处理器

如果你需要下面这些原始 KSP 选项，就直接用 [`modbus-ksp-tcp`](../modbus-ksp-tcp/README.md)：

- `addzero.modbus.address.lock.path`
- `addzero.modbus.apiClientPackageName`
- `addzero.modbus.apiClientOutputDir`

## 跨仓库本地联调

和 RTU 一样，消费仓库如果是另一个工程，建议先把插件发布到 `mavenLocal`，再继续用插件 DSL：

```bash
cd /Users/zjarlin/IdeaProjects/addzero-lib-jvm
./gradlew \
  :lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base:publishToMavenLocal \
  :lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin:publishToMavenLocal
```

这样比业务仓库手写原始 `ksp(project(...))` 更稳，也更不容易把 companion runtime 依赖漏掉。

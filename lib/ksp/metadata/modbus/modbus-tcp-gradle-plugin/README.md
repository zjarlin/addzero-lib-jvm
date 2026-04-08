# modbus-tcp-gradle-plugin

项目级 Modbus TCP KSP 消费插件。

- Plugin id: `site.addzero.ksp.modbus-tcp`
- Maven 坐标：`site.addzero:modbus-tcp-gradle-plugin`
- 本地路径：`lib/ksp/metadata/modbus/modbus-tcp-gradle-plugin`

## 作用

这个模块是 `modbus-ksp-tcp` 的推荐消费入口。

它负责：

- 应用 `com.google.devtools.ksp`
- 注入 `site.addzero:modbus-ksp-tcp`
- 自动补 `site.addzero:modbus-runtime`
  - KMP 消费者会落到 `commonMainImplementation`
  - JVM 消费者会落到 `implementation`
- 把 typed DSL 转成底层 `ksp.arg(...)`

## 用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    transports.set(listOf("tcp"))
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## 跨仓库本地联调

如果消费仓库通过 settings remap 方式引入 `../addzero-lib-jvm` 的 project path，推荐保留正常的插件 DSL：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

但要先把插件 artifact 发布到 `mavenLocal`：

```bash
cd /Users/zjarlin/IdeaProjects/addzero-lib-jvm
./gradlew \
  :lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base:publishToMavenLocal \
  :lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin:publishToMavenLocal
```

这样仍然保持项目级 Gradle plugin 入口，同时避免把整仓 `addzero-lib-jvm` 塞进 `pluginManagement.includeBuild(...)`。

当前 DSL：

- `codegenModes`
  - 会映射成 `addzero.modbus.codegen.mode`
- `contractPackages`
  - 会映射成 `addzero.modbus.contractPackages`
- `metadataProviders`
  - 会映射成 `addzero.modbus.metadata.providers`
  - 默认留空，表示让所有已发现 provider 自行判断是否启用
- `transports`
  - 会映射成 `addzero.modbus.transports`
  - 支持一次启用多个已实现 transport，例如 `listOf("rtu", "tcp")`
- `databaseDriverClass`
  - 会映射成 `addzero.modbus.database.driverClass`
- `databaseJdbcUrl`
  - 会映射成 `addzero.modbus.database.jdbcUrl`
- `databaseUsername`
  - 会映射成 `addzero.modbus.database.username`
- `databasePassword`
  - 会映射成 `addzero.modbus.database.password`
- `databaseQuery`
  - 会映射成 `addzero.modbus.database.query`
  - 支持 `${transport}` / `${transportName}` 占位符
- `databaseJsonColumn`
  - 会映射成 `addzero.modbus.database.jsonColumn`
- `cOutputProjectDir`
  - 会映射成 `addzero.modbus.c.output.projectDir`
  - 配置后会把生成的 C 文件镜像到固件工程
- `bridgeImplPath`
  - 会映射成 `addzero.modbus.c.bridgeImpl.path`
  - 控制可编辑 bridge 实现目录，默认 `Core/Src/modbus`
- `keilUvprojxPath`
  - 会映射成 `addzero.modbus.keil.uvprojx.path`
- `keilTargetName`
  - 会映射成 `addzero.modbus.keil.targetName`
- `keilGroupName`
  - 会映射成 `addzero.modbus.keil.groupName`
  - 默认 `Core/modbus/tcp`
- `mxprojectPath`
  - 会映射成 `addzero.modbus.mxproject.path`

元数据来源示例：

```kotlin
modbusTcp {
    metadataProviders.set(listOf("interfaces"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

```kotlin
modbusTcp {
    metadataProviders.set(listOf("database"))
    databaseDriverClass.set("org.sqlite.JDBC")
    databaseJdbcUrl.set("jdbc:sqlite:/absolute/path/codegen-context.db")
    databaseQuery.set("select payload from codegen_context_modbus_contract where transport = '${'$'}{transport}'")
    databaseJsonColumn.set("payload")
}
```

数据库 provider 读取的每一行都应该是 JSON 文本，支持：

- 单个 service 对象
- service 数组
- `{ "services": [...] }`

如果 TCP 场景也要落固件工程，目录约定和 RTU 一样，只是 transport 层目录切成 `tcp`：

- 请勿手动修改：
  - `Core/Inc/generated/modbus/tcp/...`
  - `Core/Src/generated/modbus/tcp/...`
- 需要接业务逻辑：
  - `Core/Src/modbus/tcp/<service>/<service>_bridge_impl.c`

项目文件同步范围：

- 会改：
  - `.uvprojx`
  - `.mxproject`
- 不会改：
  - `.uvoptx`
  - `.ioc`

## 兼容说明

底层处理器仍然是 `modbus-ksp-tcp`。

也就是说：

- 老工程继续手写 `ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))` 还能工作
- 新工程默认应该改成 `site.addzero.ksp.modbus-tcp`

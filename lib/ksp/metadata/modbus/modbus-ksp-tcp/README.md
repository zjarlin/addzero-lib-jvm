# modbus-ksp-tcp

`modbus-ksp-tcp` 是 Modbus TCP 的原始 KSP 处理器入口。

- Maven 坐标：`site.addzero:modbus-ksp-tcp`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-tcp`

## 什么时候直接用它

默认还是优先用 [`modbus-tcp-gradle-plugin`](../modbus-tcp-gradle-plugin/README.md)。

直接依赖 `modbus-ksp-tcp` 的典型理由有：

- 你要手写底层 `ksp` 参数。
- 你要用 typed DSL 还没暴露的原始选项。
  - `addzero.modbus.address.lock.path`
  - `addzero.modbus.apiClientPackageName`
  - `addzero.modbus.apiClientOutputDir`
- 你在做 processor 联调或渲染测试。

## 最小用法

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "server,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

契约接口应该使用：

- `@GenerateModbusTcpServer`
- `@ModbusOperation`
- `@ModbusParam`
- `@ModbusField`

## 模式说明

只有两个有效模式：

- `server`
  - 生成 `GeneratedModbusTcp.kt` 这类 Kotlin 网关/服务端源码。
- `contract`
  - 生成 C 合同、dispatch、bridge 示例、Markdown 协议文档。
  - 对 `database` 这类没有源码契约的 provider，会补出 Kotlin contract 和 DTO。

## metadata 输入示例

### interfaces provider

```kotlin
ksp {
    arg("addzero.modbus.metadata.providers", "interfaces")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

### database provider

```kotlin
ksp {
    arg("addzero.modbus.metadata.providers", "database")
    arg("addzero.modbus.database.driverClass", "org.sqlite.JDBC")
    arg("addzero.modbus.database.jdbcUrl", "jdbc:sqlite:/absolute/path/codegen-context.db")
    arg(
        "addzero.modbus.database.query",
        "select payload from codegen_context_modbus_contract where transport = '${'$'}{transport}'",
    )
    arg("addzero.modbus.database.jsonColumn", "payload")
}
```

## 常用附加参数

### Spring 风格源码

```kotlin
ksp {
    arg(
        "addzero.modbus.spring.route.outputDir",
        layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath,
    )
}
```

### 镜像到外部固件工程

```kotlin
ksp {
    arg("addzero.modbus.c.output.projectDir", "/absolute/path/to/firmware-project")
    arg("addzero.modbus.c.bridgeImpl.path", "Core/Src/modbus")
    arg("addzero.modbus.markdown.output.path", "Docs/generated/modbus")
}
```

### 地址锁文件

```kotlin
ksp {
    arg("addzero.modbus.address.lock.path", "/absolute/path/to/device.tcp.addresses.lock")
}
```

### API client 源码输出

```kotlin
ksp {
    arg("addzero.modbus.apiClientPackageName", "site.addzero.generated.client.modbus.tcp")
    arg("addzero.modbus.apiClientOutputDir", layout.buildDirectory.dir("generated/modbus-client").get().asFile.absolutePath)
}
```

## 常见输出

- `build/generated/ksp/main/kotlin/.../GeneratedModbusTcp.kt`
- `build/generated/ksp/main/resources/generated/modbus/tcp/*.h`
- `build/generated/ksp/main/resources/generated/modbus/tcp/*.c`
- `build/generated/ksp/main/resources/generated/modbus/protocols/*.md`

如果 metadata 不是来自源码契约，还会额外生成纯 Kotlin contract。

## 一句提醒

`modbus-ksp-tcp` 是“原始处理器入口”，不是“业务层应该默认接的模块”。

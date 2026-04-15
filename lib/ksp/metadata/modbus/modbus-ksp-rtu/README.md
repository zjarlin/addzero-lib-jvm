# modbus-ksp-rtu

`modbus-ksp-rtu` 是 Modbus RTU 的原始 KSP 处理器入口。

- Maven 坐标：`site.addzero:modbus-ksp-rtu`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-rtu`

## 什么时候直接用它

优先级先说清楚：

- 新业务工程，默认优先用 [`modbus-rtu-gradle-plugin`](../modbus-rtu-gradle-plugin/README.md)。
- 只有你明确需要手写底层 `ksp` 依赖和 `ksp { arg(...) }` 参数时，才直接用 `modbus-ksp-rtu`。

典型适用场景：

- 你需要原始 KSP 参数里还没被 typed DSL 暴露的能力。
  - 例如 `addzero.modbus.address.lock.path`
  - 例如 `addzero.modbus.apiClientPackageName`
  - 例如 `addzero.modbus.apiClientOutputDir`
- 你在做 processor 联调或测试，不想引入消费插件。

## 最小用法

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "server,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

你的契约接口应放在 `contractPackages` 指定的包下，并使用：

- `@GenerateModbusRtuServer`
- `@ModbusOperation`
- `@ModbusParam`
- `@ModbusField`

## 它现在认哪些模式

只有两个：

- `server`
  - 生成 `GeneratedModbusRtu.kt` 这类 Kotlin 网关/服务端源码。
- `contract`
  - 生成 C 头文件、C 源文件、dispatch、bridge 示例、Markdown 协议文档。
  - 对没有源码契约的 provider，还会额外生成 Kotlin contract 和 DTO。

注意：

- 没有单独的 `gateway` 模式。
- 旧用法里如果还写 `gateway`，请改成 `server`。

## metadata 输入怎么选

### 1. 从 Kotlin 接口收集

```kotlin
ksp {
    arg("addzero.modbus.metadata.providers", "interfaces")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

### 2. 从数据库 JSON 收集

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

数据库返回的 JSON 支持：

- 单个 service 对象
- service 数组
- `{ "services": [...] }`

## 常用附加参数

### Spring 风格源码输出

```kotlin
ksp {
    arg(
        "addzero.modbus.spring.route.outputDir",
        layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath,
    )
}
```

这会额外生成 `GeneratedModbusRtuSpringRoutesSource.kt`，给 `spring2ktor-server` 接着处理。

### 外部固件工程镜像输出

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
    arg(
        "addzero.modbus.address.lock.path",
        layout.projectDirectory.file("src/main/modbus/device.rtu.addresses.lock").asFile.absolutePath,
    )
}
```

### 额外生成 API client

```kotlin
ksp {
    arg("addzero.modbus.apiClientPackageName", "site.addzero.generated.client.modbus.rtu")
    arg("addzero.modbus.apiClientOutputDir", layout.buildDirectory.dir("generated/modbus-client").get().asFile.absolutePath)
}
```

## 会生成哪些东西

常见输出包括：

- `build/generated/ksp/main/kotlin/.../GeneratedModbusRtu.kt`
- `build/generated/ksp/main/resources/generated/modbus/rtu/*.h`
- `build/generated/ksp/main/resources/generated/modbus/rtu/*.c`
- `build/generated/ksp/main/resources/generated/modbus/protocols/*.md`

如果走的是 `database` provider，还可能额外生成：

- `build/generated/ksp/main/kotlin/<contractPackage>/*.kt`

## 一句提醒

`modbus-ksp-rtu` 是原始入口，不是推荐业务入口。

能用 `site.addzero.ksp.modbus-rtu` 的场景，就别让业务模块自己手写这一层细节。

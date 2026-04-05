# modbus-ksp-tcp

Modbus TCP KSP 处理器模块。

- Maven 坐标：`site.addzero:modbus-ksp-tcp`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-tcp`
- 作用：
  - 解析带注解的设备接口
  - 生成 TCP 侧 Ktor / Koin / C 产物

## 怎么接

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
    springRouteOutputDir.set(layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath)
}
```

这个插件会自动：

- 应用 `com.google.devtools.ksp`
- 注入 `modbus-ksp-tcp`
- 注入 `modbus-runtime`

如果你需要手动控制底层依赖，仍然可以继续使用原始 `ksp(...)` 接法。

### 在契约源码模块里生成 C 壳子

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

生成位置：

- `build/generated/ksp/main/resources/generated/modbus/tcp/*.h`
- `build/generated/ksp/main/resources/generated/modbus/tcp/*.c`

### 在 server 模块里生成 TCP 路由与网关

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "server")
    arg("addzero.modbus.contractPackages", "site.addzero.esp32_host_computer.api")
}
```

生成位置：

- `build/generated/ksp/main/kotlin/site/addzero/esp32_host_computer/generated/modbus/tcp/GeneratedModbusTcp.kt`

### 生成 Spring2Ktor 风格的路由源码

如果你希望把最终的 Ktor 路由注册交给 `spring2ktor-server`，可以额外指定 Spring 路由源码输出根目录：

```kotlin
modbusTcp {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
    springRouteOutputDir.set(layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath)
}
```

这时会额外输出：

- `<springRouteOutputDir>/site/addzero/esp32_host_computer/generated/modbus/tcp/GeneratedModbusTcpSpringRoutesSource.kt`

配置了 `springRouteOutputDir` 之后，`GeneratedModbusTcp.kt` 不再内嵌直接的 `Route.registerGeneratedModbusTcpRoutes()`。

### 一次同时生成服务端与契约产物

```kotlin
ksp {
    arg("addzero.modbus.codegen.mode", "server,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

生成位置：

- `build/generated/ksp/main/kotlin/site/addzero/esp32_host_computer/generated/modbus/tcp/GeneratedModbusTcp.kt`
- `build/generated/ksp/main/resources/generated/modbus/tcp/*.h`
- `build/generated/ksp/main/resources/generated/modbus/tcp/*.c`
- `build/generated/ksp/main/resources/generated/modbus/tcp/modbus_tcp_dispatch.h`
- `build/generated/ksp/main/resources/generated/modbus/tcp/modbus_tcp_dispatch.c`
- `build/generated/ksp/main/resources/generated/modbus/protocols/*.md`

## 生成内容

- `GeneratedModbusTcpKoinModule`
- 每个契约接口对应一个 `GeneratedTcpGateway`
- 每个契约接口同时生成一个 Koin 接口绑定，允许业务直接按原接口类型注入
- 每个操作对应一个请求 DTO
- 可选的 `GeneratedModbusTcpSpringRoutesSource.kt`

## 使用提醒

- TCP 处理器和 RTU 处理器共享同一套注解与 IR。
- 契约接口本身应该放在业务模块里，不要再单独造一个 `device-contract-api` 公共壳模块。

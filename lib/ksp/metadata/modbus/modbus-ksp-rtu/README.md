# modbus-ksp-rtu

Modbus RTU KSP 处理器模块。

- Maven 坐标：`site.addzero:modbus-ksp-rtu`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-rtu`
- 作用：
  - 解析带注解的设备接口
  - 生成 RTU 侧 Kotlin gateway / Koin / C 产物

## 怎么接

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("gateway"))
    contractPackages.set(listOf("site.addzero.device.contract"))
    springRouteOutputDir.set(layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath)
}
```

这个插件会自动：

- 应用 `com.google.devtools.ksp`
- 注入 `modbus-ksp-rtu`
- 注入 `modbus-runtime`

如果你需要手动控制底层依赖，仍然可以继续使用原始 `ksp(...)` 接法。

### 在契约源码模块里生成 C 壳子

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

生成位置：

- `build/generated/ksp/main/resources/generated/modbus/rtu/*.h`
- `build/generated/ksp/main/resources/generated/modbus/rtu/*.c`

### 在 gateway 模块里生成 RTU 路由与网关

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "gateway")
    arg("addzero.modbus.contractPackages", "site.addzero.esp32_host_computer.api")
}
```

生成位置：

- `build/generated/ksp/main/kotlin/site/addzero/esp32_host_computer/generated/modbus/rtu/GeneratedModbusRtu.kt`

### 生成 Spring2Ktor 风格的路由源码

当你希望让 `spring2ktor-server` 再接手生成最终的 Ktor `Route` 注册代码时，可以额外指定 Spring 路由源码输出根目录：

```kotlin
modbusRtu {
    codegenModes.set(listOf("gateway"))
    contractPackages.set(listOf("site.addzero.device.contract"))
    springRouteOutputDir.set(layout.buildDirectory.dir("generated/modbus-spring-routes").get().asFile.absolutePath)
}
```

这时会额外输出：

- `<springRouteOutputDir>/site/addzero/esp32_host_computer/generated/modbus/rtu/GeneratedModbusRtuSpringRoutesSource.kt`

注意：

- 这份源码是给 `spring2ktor-server` 继续处理的 Spring 风格顶层 handler。
- 配置了 `springRouteOutputDir` 之后，`GeneratedModbusRtu.kt` 不再内嵌直接的 `Route.registerGeneratedModbusRtuRoutes()`。

### 一次同时生成 gateway 与契约产物

```kotlin
ksp {
    arg("addzero.modbus.codegen.mode", "gateway,contract")
    arg("addzero.modbus.contractPackages", "site.addzero.device.contract")
}
```

生成位置：

- `build/generated/ksp/main/kotlin/site/addzero/esp32_host_computer/generated/modbus/rtu/GeneratedModbusRtu.kt`
- `build/generated/ksp/main/resources/generated/modbus/rtu/*.h`
- `build/generated/ksp/main/resources/generated/modbus/rtu/*.c`
- `build/generated/ksp/main/resources/generated/modbus/rtu/modbus_rtu_dispatch.h`
- `build/generated/ksp/main/resources/generated/modbus/rtu/modbus_rtu_dispatch.c`
- `build/generated/ksp/main/resources/generated/modbus/protocols/*.md`

## 生成内容

- `GeneratedModbusRtuKoinModule`
- 每个契约接口对应一个 `GeneratedRtuGateway`
- 每个契约接口同时生成一个 Koin 接口绑定，例如 `fun deviceApi(gateway: DeviceApiGeneratedRtuGateway): DeviceApi = gateway`
- 每个操作对应一个请求 DTO
- 可选的 `GeneratedModbusRtuSpringRoutesSource.kt`

## 使用提醒

- 契约接口本身应该放在业务模块里，不要再单独造一个 `device-contract-api` 公共壳模块。
- 处理器只扫描你通过 `addzero.modbus.contractPackages` 指定的包列表。
- RTU 默认配置现在建议由业务自己通过 Koin 提供一份 `ModbusRtuEndpointConfig` 实现。

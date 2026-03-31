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
- `registerGeneratedModbusRtuRoutes()`
- 每个契约接口对应一个 `GeneratedRtuGateway`
- 每个操作对应一个请求 DTO

## 使用提醒

- 契约接口本身应该放在业务模块里，不要再单独造一个 `device-contract-api` 公共壳模块。
- 处理器只扫描你通过 `addzero.modbus.contractPackages` 指定的包列表。

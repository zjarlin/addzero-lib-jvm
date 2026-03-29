# modbus-ksp-tcp

Modbus TCP KSP 处理器模块。

- Maven 坐标：`site.addzero:modbus-ksp-tcp`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-tcp`
- 作用：
  - 解析带注解的设备接口
  - 生成 TCP 侧 Ktor / Koin / C 产物

## 怎么接

```kotlin
dependencies {
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))
}
```

### 在契约源码模块里生成 C 壳子

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
    ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))
}

ksp {
    arg("addzero.modbus.codegen.mode", "contract")
    arg("addzero.modbus.contractPackage", "site.addzero.device.contract")
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
    arg("addzero.modbus.contractPackage", "site.addzero.esp32_host_computer.api")
}
```

生成位置：

- `build/generated/ksp/main/kotlin/site/addzero/esp32_host_computer/generated/modbus/tcp/GeneratedModbusTcp.kt`

### 一次同时生成服务端与契约产物

```kotlin
ksp {
    arg("addzero.modbus.codegen.mode", "server,contract")
    arg("addzero.modbus.contractPackage", "site.addzero.device.contract")
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
- `registerGeneratedModbusTcpRoutes()`
- 每个契约接口对应一个 `GeneratedTcpGateway`
- 每个操作对应一个请求 DTO

## 使用提醒

- TCP 处理器和 RTU 处理器共享同一套注解与 IR。
- 契约接口本身应该放在业务模块里，不要再单独造一个 `device-contract-api` 公共壳模块。

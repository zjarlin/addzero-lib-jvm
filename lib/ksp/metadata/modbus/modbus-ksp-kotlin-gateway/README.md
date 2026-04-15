# modbus-ksp-kotlin-gateway

`modbus-ksp-kotlin-gateway` 是 Kotlin gateway 产物生成器模块，它通过 `ServiceLoader` 向 `modbus-ksp-core` 注册 `KOTLIN_GATEWAY` 生成器。

- Maven 坐标：`site.addzero:modbus-ksp-kotlin-gateway`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-kotlin-gateway`

## 它生成什么

这个模块负责生成 transport 对应的 Kotlin 网关/服务端源码，例如：

- `GeneratedModbusRtu.kt`
- `GeneratedModbusTcp.kt`
- `GeneratedModbusMqtt.kt`

典型内容包括：

- `GeneratedModbus*KoinModule`
- 每个 service 对应的 `*Generated*Gateway`
- transport 配置注册表的读取与默认配置接线
- 每个操作的请求参数适配与 executor 调用

## 谁应该直接依赖它

通常不是业务模块。

直接依赖它的应该是：

- `modbus-ksp-rtu`
- `modbus-ksp-tcp`
- `modbus-ksp-mqtt`
- 自己组装 Modbus processor 的模块

## 最小接法

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-core"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway"))
}
```

## 业务层是怎么“使用”这个模块的

业务层通常通过更高层入口间接使用它：

- `site.addzero.ksp.modbus-rtu`
- `site.addzero.ksp.modbus-tcp`
- `modbus-ksp-mqtt`

当你把 `codegenModes` 里包含 `server` 时，这个模块就会参与生成。

## 一个关键边界

如果你额外配置了：

- `addzero.modbus.spring.route.outputDir`

那主 gateway 文件仍会生成，但路由注册策略会切到“输出 Spring 风格源码，再交给 `spring2ktor-server`”的模式。

也就是说，这个模块负责主网关文件，但它的行为会被上层 processor 传入的 `serverRouteMode` 影响。

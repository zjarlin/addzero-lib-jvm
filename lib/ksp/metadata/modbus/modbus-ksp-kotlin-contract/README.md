# modbus-ksp-kotlin-contract

`modbus-ksp-kotlin-contract` 是 Modbus Kotlin contract 生成器模块，它通过 `ServiceLoader` 向 `modbus-ksp-core` 注册 `KOTLIN_CONTRACT` 产物生成器。

- Maven 坐标：`site.addzero:modbus-ksp-kotlin-contract`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-kotlin-contract`

## 它到底生成什么

当处理器进入 `contract` 模式，并且 metadata 来源本身没有 Kotlin 契约源码时，这个模块会生成：

- Kotlin `interface`
- DTO `data class`

典型场景就是 `database` provider：

- 数据库里只有 JSON 元数据，没有手写 Kotlin 接口。
- 这时 `modbus-ksp-kotlin-contract` 会补出纯 Kotlin contract，供后面的 server/gateway 编译。

如果 metadata 来源是 `interfaces` provider，就要注意：

- 你本来已经有手写契约接口。
- 这个模块不会重复生成同名 Kotlin 接口。

## 谁应该直接依赖它

通常不是业务模块。

直接依赖它的应该是：

- `modbus-ksp-rtu`
- `modbus-ksp-tcp`
- `modbus-ksp-mqtt`
- 你自己写的自定义 Modbus processor 组合模块
- 针对 `modbus-ksp-core` 的渲染测试模块

## 最小接法

如果你在组装自己的 processor 模块，可以这样接：

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-core"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-contract"))
}
```

只要这个模块出现在处理器 classpath 上，`ModbusArtifactRenderer` 就能通过 `ServiceLoader` 找到它。

## 普通业务工程要不要碰它

不要。

普通业务工程直接用：

- `site.addzero.ksp.modbus-rtu`
- `site.addzero.ksp.modbus-tcp`
- 或原始处理器 `modbus-ksp-mqtt`

这些入口已经把 `modbus-ksp-kotlin-contract` 作为处理器内部依赖带上了。

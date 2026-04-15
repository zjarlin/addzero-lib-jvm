# Modbus Metadata Suite

`lib/ksp/metadata/modbus` 不是一个单独的 KSP 模块，而是一整套 Modbus 协议元数据与代码生成模块。

这套目录里现在有 16 个子模块，职责分成四层：

- 业务接入层
  - 给业务工程直接接的入口，优先用 `modbus-rtu-gradle-plugin`、`modbus-tcp-gradle-plugin`，运行时配合 `modbus-runtime`。
- 原始处理器层
  - `modbus-ksp-rtu`、`modbus-ksp-tcp`、`modbus-ksp-mqtt`，用于你必须手写 `ksp { arg(...) }` 的场景。
- SPI 扩展层
  - `modbus-ksp-core`、`modbus-ksp-kotlin-contract`、`modbus-ksp-kotlin-gateway`、`modbus-ksp-c-contract`、`modbus-ksp-markdown`、`modbus-ksp-keil-sync`。
  - 这一层主要给“组装处理器”或“扩展生成链路”的人用，不是普通业务模块直接依赖的入口。
- 纯模型/纯工具层
  - `modbus-codegen-model`、`modbus-codegen-core`，用于 JSON 元数据、默认值推导、无 KSP 的纯 Kotlin contract 生成。

## 先看怎么选

| 你的目标 | 推荐模块 |
| --- | --- |
| 给业务模块接 RTU 生成 | [`modbus-rtu-gradle-plugin`](./modbus-rtu-gradle-plugin/README.md) + [`modbus-runtime`](./modbus-runtime/README.md) |
| 给业务模块接 TCP 生成 | [`modbus-tcp-gradle-plugin`](./modbus-tcp-gradle-plugin/README.md) + [`modbus-runtime`](./modbus-runtime/README.md) |
| 给业务模块接 MQTT 生成 | 优先评估原始处理器 [`modbus-ksp-mqtt`](./modbus-ksp-mqtt/README.md)；仓库内也提供 [`modbus-mqtt-gradle-plugin`](./modbus-mqtt-gradle-plugin/README.md) 这个便利入口 |
| 只想要注解、编码器、RTU/TCP/MQTT 运行时抽象 | [`modbus-runtime`](./modbus-runtime/README.md) |
| 自己扩展 metadata provider | [`modbus-ksp-core`](./modbus-ksp-core/README.md) |
| 自己扩展某类输出产物 | [`modbus-ksp-core`](./modbus-ksp-core/README.md) + 对应 artifact 模块 |
| 只想在 Kotlin/JVM 里解析/生成标准 Modbus 元数据 JSON | [`modbus-codegen-model`](./modbus-codegen-model/README.md)、[`modbus-codegen-core`](./modbus-codegen-core/README.md) |
| 验证整条 RTU 生成链路是否还正常 | [`modbus-ksp-rtu-smoke`](./modbus-ksp-rtu-smoke/README.md) |

## 业务接入的默认路径

### RTU

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

### TCP

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

### MQTT

如果你只是仓库内联调，或者已经确认要沿用现成的插件包装，可以直接用：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-mqtt")
}

modbusMqtt {
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

但要说清楚一件事：

- 仓库级 KSP policy 当前明确保留的默认 published consumer plugin 是 `modbus-rtu` 和 `modbus-tcp`。
- `modbus-mqtt-gradle-plugin` 这个模块虽然存在，也能在仓库里使用，但它不是当前 policy 文档里列出的“默认推荐对外发布入口”。
- 所以新项目要大范围推广 MQTT 接法时，先确认发布策略；只在本仓库或受控消费仓库里用它没有问题。

## 这套生成链现在到底认哪些模式

只有两个模式：

- `server`
  - 生成 Kotlin 网关/服务端源码，例如 `GeneratedModbusRtu.kt`、`GeneratedModbusTcp.kt`、`GeneratedModbusMqtt.kt`。
- `contract`
  - 生成 C 头文件、C dispatch、bridge 示例、Markdown 协议文档。
  - 如果 metadata 来源本身没有 Kotlin 契约源码，比如数据库 provider，还会额外生成纯 Kotlin contract 接口和 DTO。

注意：

- 现在没有单独的 `gateway` 模式。
- 旧文档里把 `gateway` 当作主模式的地方，已经不符合当前代码。

## 现在支持哪些 metadata 输入源

所有 transport processor 都通过 `ServiceLoader<ModbusMetadataProvider>` 找输入源，默认内置两个 provider：

- `interfaces`
  - 从带 `@GenerateModbusRtuServer` / `@GenerateModbusTcpServer` / `@GenerateModbusMqttServer` 注解的 Kotlin 接口收集元数据。
  - 需要 `contractPackages`。
- `database`
  - 通过 JDBC 执行查询，把 JSON payload 解析成统一的 `ModbusServiceModel`。
  - 需要 `databaseJdbcUrl` 和 `databaseQuery`。

不显式配置 `metadataProviders` 时，processor 会把 classpath 里能发现的 provider 都加载出来，再由每个 provider 自己判断是否启用。

## 原始 KSP 常用参数

无论你接的是 RTU、TCP 还是 MQTT 原始处理器，下面这些参数名都通用：

- `addzero.modbus.codegen.mode`
  - `server`、`contract`，逗号分隔。
- `addzero.modbus.contractPackages`
  - 扫描契约接口的包列表。
- `addzero.modbus.metadata.providers`
  - 例如 `interfaces`、`database`。
- `addzero.modbus.database.*`
  - JDBC 元数据输入。
- `addzero.modbus.c.output.projectDir`
  - 把生成的 C/Markdown 额外镜像到外部固件工程。
- `addzero.modbus.c.bridgeImpl.path`
  - 可编辑 `*_bridge_impl.c` 的落点，默认 `Core/Src/modbus`。
- `addzero.modbus.markdown.output.path`
  - 外部固件工程里 Markdown 文档目录，默认 `Docs/generated/modbus`。
- `addzero.modbus.spring.route.outputDir`
  - 额外输出 Spring 风格源码，给 `spring2ktor-server` 继续处理。
- `addzero.modbus.address.lock.path`
  - 地址锁文件，避免自动分配的协议地址在重构后漂移。
- `addzero.modbus.apiClientPackageName`
  - 额外生成 Ktorfit client 的目标包名。
- `addzero.modbus.apiClientOutputDir`
  - 额外生成 Ktorfit client 的目标输出目录。

## 子模块索引

| 模块 | 角色 | 普通业务工程要不要直接依赖 | 说明 |
| --- | --- | --- | --- |
| [`modbus-runtime`](./modbus-runtime/README.md) | 注解 + 运行时抽象 + RTU/TCP 执行器 | 要 | 所有业务接入都绕不过它 |
| [`modbus-rtu-gradle-plugin`](./modbus-rtu-gradle-plugin/README.md) | RTU 推荐入口 | 要 | 新业务工程优先用它 |
| [`modbus-tcp-gradle-plugin`](./modbus-tcp-gradle-plugin/README.md) | TCP 推荐入口 | 要 | 新业务工程优先用它 |
| [`modbus-mqtt-gradle-plugin`](./modbus-mqtt-gradle-plugin/README.md) | MQTT 便利入口 | 视情况 | 仓库内可用，但不在当前默认保留 plugin 清单里 |
| [`modbus-ksp-rtu`](./modbus-ksp-rtu/README.md) | RTU 原始处理器 | 一般不要 | 需要手写原始 `ksp` 参数时再用 |
| [`modbus-ksp-tcp`](./modbus-ksp-tcp/README.md) | TCP 原始处理器 | 一般不要 | 同上 |
| [`modbus-ksp-mqtt`](./modbus-ksp-mqtt/README.md) | MQTT 原始处理器 | 一般不要 | 同上 |
| [`modbus-ksp-core`](./modbus-ksp-core/README.md) | metadata / artifact / project-sync SPI 核心 | 不要 | 扩展链路时才碰 |
| [`modbus-ksp-kotlin-contract`](./modbus-ksp-kotlin-contract/README.md) | Kotlin contract 生成器 | 不要 | 通常由处理器传递引入 |
| [`modbus-ksp-kotlin-gateway`](./modbus-ksp-kotlin-gateway/README.md) | Kotlin gateway 生成器 | 不要 | 通常由处理器传递引入 |
| [`modbus-ksp-c-contract`](./modbus-ksp-c-contract/README.md) | C 合同与 dispatch 生成器 | 不要 | 通常由处理器传递引入 |
| [`modbus-ksp-markdown`](./modbus-ksp-markdown/README.md) | Markdown 协议文档生成器 | 不要 | 通常由处理器传递引入 |
| [`modbus-ksp-keil-sync`](./modbus-ksp-keil-sync/README.md) | Keil / CubeMX 工程同步 SPI | 不要 | 只有外部固件工程同步才用到 |
| [`modbus-codegen-model`](./modbus-codegen-model/README.md) | 纯模型与序列化 schema | 视情况 | 适合外部工具复用 |
| [`modbus-codegen-core`](./modbus-codegen-core/README.md) | 纯 Kotlin 解析/校验/contract 生成工具 | 视情况 | 适合非 KSP 流程复用 |
| [`modbus-ksp-rtu-smoke`](./modbus-ksp-rtu-smoke/README.md) | 冒烟测试模块 | 不要 | 只给仓库自己验证整链路 |

## 一个容易踩坑的边界

`transports` 不是“一个处理器生成多个传输层”的万能开关，它只是“当前已接入的那个处理器是否启用”的开关。

例如：

- `site.addzero.ksp.modbus-rtu` 只会把 `modbus-ksp-rtu` 注入 classpath。
- 你就算写 `transports.set(listOf("rtu", "tcp"))`，它也不会顺带把 TCP 生成出来。
- 真要同时生成 RTU 和 TCP，必须把两个 transport 的处理器都接进来。

## 建议的阅读顺序

1. 先看 [`modbus-runtime`](./modbus-runtime/README.md) 确认注解和运行时模型。
2. 再看你要接入的 transport 插件 README。
3. 只有当你需要原始参数或扩展 SPI 时，才继续看 `modbus-ksp-*` 和 `modbus-codegen-*` 这些内部模块。

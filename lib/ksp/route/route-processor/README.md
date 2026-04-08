# route-processor

`route-processor` 用于收集 `@Route` 元数据，并在多模块场景下按 owner/contributor 模式聚合生成路由代码：

- contributor 模块：只写当前模块 snapshot，不直接产出最终 `RouteKeys.kt` / `RouteTable.kt`
- owner 模块：读取全部 snapshot，统一生成最终 `RouteKeys.kt` 与 `RouteTable.kt`

当前推荐把最终生成产物都落到 owner 模块源码目录，避免再把聚合结果写进共享模块，导致主应用与插件模块的职责边界不清。

## Route 元数据模型

当前推荐通过 `placement` 承载导航语义，而不是继续扩展顶层 `scene*` 字段：

```kotlin
@Route(
    title = "用户列表",
    routePath = "system/users",
    placement = RoutePlacement(
        scene = RouteScene(
            id = "system",
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
        menuPath = ["用户中心"],
        defaultInScene = true,
    ),
)
```

- `Route.value` 已降级为兼容字段，不再建议作为菜单分组语义使用
- `placement.scene` 定义顶部场景切换所需元数据
- `placement.menuPath` 定义场景内侧边栏分组
- `placement.defaultInScene` 用于显式标记场景默认页

## 支持的符号

当前会处理以下带 `@Route` 的声明：

- `class`
- `fun`
- `val` / `var`

因此顶层函数写法也会生成，不要求一定写在类里。

## 处理流程

处理器采用两阶段工作流：

1. `process()` 只做当前模块的 `@Route` 元数据收集
2. `finish()` 先写当前模块 snapshot；如果当前模块是 owner，再继续合并全部 snapshot 并生成最终结果

跨模块 snapshot 保存在：

```text
<routeOwnerModule>/../build/addzero/route-processor/<routeGenPkg as path>/snapshots
```

推荐显式传 `routeModuleKey` 作为 snapshot key。未传时，处理器会退回到基于 source roots 推导的模块 key。

## 输出规则

- owner 模块会把 `RouteKeys.kt` 与 `RouteTable.kt` 都生成到 `routeOwnerModule`
- contributor 模块不会生成最终路由文件，只会更新自己的 snapshot
- `routeOwnerModule` 必须是绝对源码目录

如果 `routeOwnerModule` 为空，或不是绝对路径，处理器会跳过最终聚合，只保留 snapshot 更新。

## 必填 KSP 参数

### `routeGenPkg`

所有参与聚合的模块都必须传相同值。

- 含义：生成代码包名
- 用于：`RouteKeys.kt` / `RouteTable.kt` 包名与 snapshot 分组目录

### `routeOwnerModule`

所有参与同一套路由聚合的模块都应该传相同值。

- 含义：owner 模块源码目录绝对路径
- 用于：保存 snapshot 根目录，并在 owner 模块生成最终 `RouteKeys.kt` / `RouteTable.kt`
- 不是：Gradle 的 `project.path`

### `routeAggregationRole`

所有参与聚合的模块都应该传此参数。

- 可选值：`contributor`、`owner`
- contributor：只写 snapshot
- owner：写 snapshot 并生成最终路由文件

### `routeModuleKey`

建议所有参与聚合的模块都显式传此参数。

- 含义：当前模块 snapshot 的稳定 key
- 推荐值：Gradle `project.path`

### `sharedSourceDir`

这是兼容旧接入方式的遗留参数，新的 owner/contributor 模型不再依赖它。

- 当前行为：如果传了旧 `sharedSourceDir`，owner 聚合完成后会尝试清理其中残留的旧 `RouteKeys.kt` / `RouteTable.kt`
- 建议：新接入不要再传

## Gradle 配置示例

推荐直接使用消费插件。

### contributor 模块

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("site.addzero.ksp.route")
}

val routeOwnerModuleDir = project(":app-desktop")
    .extensions
    .getByType<KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath

route {
    generatedPackage.set("site.addzero.generated")
    routeOwnerModule.set(routeOwnerModuleDir)
    aggregationRole.set("contributor")
    moduleKey.set(project.path)
}
```

### owner 模块

```kotlin
plugins {
    id("site.addzero.ksp.route")
}

val ownerSourceDir = kotlin
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath

route {
    generatedPackage.set("site.addzero.generated")
    routeOwnerModule.set(ownerSourceDir)
    aggregationRole.set("owner")
    moduleKey.set(project.path)
}
```

消费插件会自动：

- 应用 `com.google.devtools.ksp`
- 注入 `route-processor`
- 注入 `route-core`
- 根据 JVM / KMP 模块类型选择正确的 KSP configuration

如果你需要最低层手动控制，也可以直接写 KSP 参数：

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val routeOwnerModuleDir = project(":app-desktop")
    .extensions
    .getByType<KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath

ksp {
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeAggregationRole", "contributor")
    arg("routeModuleKey", project.path)
}
```

如果是纯 JVM 模块，也可以直接从 `SourceSetContainer` 取 `main` 源码目录，关键点只有一个：传绝对路径，不要传 `:module-name`。

## 多模块聚合约束

假设有 `A`、`B`、`C` 三个模块都声明了 `@Route`：

- `A`、`B`、`C` 都要启用这个 KSP processor
- `A`、`B`、`C` 的 `routeGenPkg` 必须一致
- `A`、`B`、`C` 的 `routeOwnerModule` 必须都指向同一个 owner 模块源码目录
- 只有其中一个模块应该设置 `routeAggregationRole=owner`
- 其余模块都设置 `routeAggregationRole=contributor`
- owner 模块必须直接依赖所有 contributor 模块，否则生成出来的 `RouteTable.kt` 无法编译

最终结果是：

- 所有 contributor 只更新自己的 snapshot
- owner 统一生成 `RouteKeys.kt`
- owner 统一生成 `RouteTable.kt`

## 冲突规则

### 重复 `routePath`

如果不同声明生成了相同的 `routePath`，聚合阶段会记录 warning，并按最后写入的快照结果覆盖。

### 重复 `scene.id`

如果多个路由声明了同一个 `scene.id`，它们的 `scene.name` / `scene.icon` / `scene.order` 必须完全一致，否则聚合阶段会直接失败。

### 重复 `defaultInScene`

同一个 `scene.id` 只能有一个 `defaultInScene = true`，否则聚合阶段会直接失败。

### 重复 Route 常量名

如果多个声明的简单名生成了相同的常量名，处理器会自动回退为基于限定名的常量名，避免 `RouteKeys` 常量冲突。

## 迁移说明

旧行为会把聚合结果写到共享模块目录。现在已经改为：

- 所有最终产物都写到 owner 模块目录
- 共享目录不再承载最终 `RouteKeys.kt` / `RouteTable.kt`
- 旧 `sharedSourceDir` 只用于清理历史残留

处理器在 owner 聚合完成后，会尝试清理旧 `sharedSourceDir` 中残留的 `RouteKeys.kt` / `RouteTable.kt`，避免同包名重复类残留。

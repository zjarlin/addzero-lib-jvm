# route-processor

`route-processor` 用于收集 `@Route` 元数据，并在多模块场景下聚合生成两类文件：

- `RouteKeys.kt`：生成到 `sharedSourceDir`
- `RouteTable.kt`：生成到 `routeOwnerModule`

这里的设计目标是把“可共享的路由常量”与“真正持有 Composable 映射表的 owner 模块”拆开，避免跨模块直接把 `RouteTable` 落到公共源码目录。

## 支持的符号

当前会处理以下带 `@Route` 的声明：

- `class`
- `fun`
- `val` / `var`

因此顶层函数写法也会生成，不要求一定写在类里。

## 处理流程

处理器采用两阶段工作流：

1. `process()` 只做当前模块的 `@Route` 元数据收集
2. `finish()` 把当前模块快照写入共享目录，再合并所有模块快照统一生成结果

跨模块快照保存在：

```text
<sharedSourceDir>/.addzero/route-processor/<routeGenPkg as path>/snapshots
```

模块快照 key 不再依赖 `routeOwnerModule`，而是根据当前编译模块的 source roots 自动推导，这样多个业务模块即使共用同一个 `routeOwnerModule`，也不会互相覆盖快照。

## 输出规则

### `RouteKeys.kt`

- 输出目录：`sharedSourceDir`
- 用途：共享路由常量与 `allMeta`

### `RouteTable.kt`

- 输出目录：`routeOwnerModule`
- 用途：持有 `RouteKeys.xxx -> @Composable { ... }` 的实际映射
- 要求：`routeOwnerModule` 必须是绝对源码目录

如果 `routeOwnerModule` 为空，或不是绝对路径，处理器会跳过 `RouteTable` 生成，但仍然会继续生成 `RouteKeys.kt`。

## 必填 KSP 参数

### `sharedSourceDir`

所有参与聚合的模块都必须传相同值。

- 含义：共享源码目录绝对路径
- 用于：保存快照、生成 `RouteKeys.kt`

### `routeGenPkg`

所有参与聚合的模块都必须传相同值。

- 含义：生成代码包名
- 用于：`RouteKeys.kt` / `RouteTable.kt` 的包名与快照分组目录

### `routeOwnerModule`

所有参与同一套路由聚合的模块都应该传相同值。

- 含义：`RouteTable.kt` 所属模块的源码目录绝对路径
- 用于：生成 `RouteTable.kt`
- 不是：Gradle 的 `project.path`

## Gradle 配置示例

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val sharedSourceDir = project(":shared-route")
    .extensions
    .getByType<KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath

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
    arg("sharedSourceDir", sharedSourceDir)
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
}
```

如果是纯 JVM 模块，也可以直接从 `SourceSetContainer` 取 `main` 源码目录，关键点只有一个：传绝对路径，不要传 `:module-name`。

## 多模块聚合约束

假设有 `A`、`B`、`C` 三个模块都声明了 `@Route`：

- `A`、`B`、`C` 都要启用这个 KSP processor
- `A`、`B`、`C` 的 `sharedSourceDir` 必须一致
- `A`、`B`、`C` 的 `routeGenPkg` 必须一致
- `A`、`B`、`C` 的 `routeOwnerModule` 应该都指向同一个 owner 模块源码目录

最终结果是：

- `RouteKeys.kt` 统一写到共享目录
- `RouteTable.kt` 统一写到 owner 模块目录

## 冲突规则

### 重复 `routePath`

如果不同声明生成了相同的 `routePath`，聚合阶段会记录 warning，并按最后写入的快照结果覆盖。

### 重复 Route 常量名

如果多个声明的简单名生成了相同的常量名，处理器会自动回退为基于限定名的常量名，避免 `RouteKeys` 常量冲突。

## 迁移说明

旧行为会把 `RouteTable.kt` 也写到 `sharedSourceDir`。现在已经改为：

- `RouteKeys.kt` 保持在 `sharedSourceDir`
- `RouteTable.kt` 改到 `routeOwnerModule`

处理器在生成新的 owner 版 `RouteTable.kt` 时，会尝试清理共享目录中的旧 `RouteTable.kt`，避免同包名重复类残留。

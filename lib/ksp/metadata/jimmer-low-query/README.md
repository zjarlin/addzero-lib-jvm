# Jimmer Low Query KSP

根据 Jimmer 实体字段上的注解生成强类型低代码查询函数，既能生成 `KMutableRootQuery.ForEntity<E>` 扩展，也能生成接收实体对象的 `KSqlClient` 查询入口。

## 推荐用法（插件优先）

```kotlin
plugins {
    id("site.addzero.ksp.jimmer-low-query")
}

jimmerLowQuery {
    // 可选：默认生成到 `<实体包>.generated.lowquery`
    generatedPackage.set("com.example.generated.lowquery")
}
```

实体示例：

```kotlin
@JimmerLowQuery
@Entity
interface SystemConfig {
    @Id
    @OrderByDesc(priority = 0)
    val id: Long

    @Eq(name = "key")
    val configKey: String
}
```

生成查询块扩展等价于：

```kotlin
public fun KMutableRootQuery.ForEntity<SystemConfig>.query(
    key: String,
): KConfigurableRootQuery<KNonNullTable<SystemConfig>, SystemConfig> {
    where(table.configKey `eq?` key)
    orderBy(table.id.desc())
    return select(table.fetchBy { allScalarFields() })
}
```

同时会生成一个 `KSqlClient` 扩展，调用方只需要传实体入参；实体类型由生成函数签名固定，字段、操作符、参数名、fetcher 等元信息全部来自实体注解：

```kotlin
val query = sqlClient.createLowQuery(
    entity = entity,
)
```

生成结果等价于：

```kotlin
@JvmName("createLowQueryForSystemConfigByEntity")
public fun KSqlClient.createLowQuery(
    entity: SystemConfig,
): KConfigurableRootQuery<KNonNullTable<SystemConfig>, SystemConfig> {
    return createQuery(SystemConfig::class) {
        if (ImmutableObjects.isLoaded(entity, "configKey")) {
            where(table.configKey `eq?` entity.configKey)
        }
        orderBy(table.id.desc())
        select(table.fetchBy { allScalarFields() })
    }
}
```

实体入参建议用 Jimmer 草稿对象，只设置需要参与查询的字段；生成代码会用 `ImmutableObjects.isLoaded(entity, "字段名")` 判断字段是否已设置，未设置字段不会参与 where。

如果消费模块存在 Spring `@Component`，处理器还会为每个实体生成一个 `JimmerLowQueryProvider<E>` 组件，并用实体 `KClass` 作为运行时关联键。这样 `crud-base` 这类上游通用模块不需要静态 import 下游实体包里的 `createLowQuery` 扩展，也能在运行时从 Spring IoC 中找到对应实体的查询逻辑。

需要注意：`createLowQuery` 本身是实体包下的 Kotlin 扩展函数，普通跨模块调用要求“生成代码所在模块在 classpath 上，并在调用文件显式 import 对应 generated 包”。上游通用库无法反向看到下游实体模块，所以泛型 `BaseController<E>` 场景应走 Spring provider 注册表。

## 注解

- `@Eq(name = "", nullable = false)`：等值查询，生成动态谓词 ``table.xxx `eq?` param``。
- `@Like(name = "", nullable = false)`：模糊查询，生成动态谓词 ``table.xxx.`ilike?`(param, LikeMode.ANYWHERE)``。
- `@In(name = "", nullable = false)`：集合包含查询，生成动态谓词 ``table.xxx `valueIn?` params``；未指定 `name` 时默认用字段名简单复数形式。
- `@OrderByAsc(priority = 0)` / `@OrderByDesc(priority = 0)`：排序注解，`priority` 越小越先排序，生成 `orderBy(...)`。
- `@JimmerLowQuery(functionName = "query", clientFunctionName = "createLowQuery", visibility = PUBLIC, clientVisibility = PUBLIC, fetcher = ALL_SCALAR_FIELDS)`：可选，标在 `@Entity` 接口上，用于覆盖生成函数配置。
- `@JimmerLowQueryParam(name = "", operator = EQ, nullable = false)`：兼容旧写法；新代码优先使用字段语义注解。

支持的字段语义注解：`@Eq`、`@Ne`、`@Like`、`@StartsWith`、`@EndsWith`、`@Gt`、`@Ge`、`@Lt`、`@Le`、`@In`、`@NotIn`。排序注解：`@OrderByAsc`、`@OrderByDesc`。

类上不写 `@JimmerLowQuery` 也可以，只要字段上有查询注解就会按默认配置生成：

```kotlin
@Entity
interface SystemConfig {
    @Eq(name = "key")
    val configKey: String
}
```

多字段示例：

```kotlin
@JimmerLowQuery(functionName = "queryByFilter")
@Entity
interface Device {
    @Like(nullable = true)
    val name: String

    @In
    val id: Long
}
```

上面 `id` 会生成参数 `ids: Collection<Long>`。

排序示例：

```kotlin
@Entity
interface PowerRuntimeStatus {
    @OrderByDesc(priority = 0)
    val snapshotTime: LocalDateTime

    @OrderByDesc(priority = 1)
    val id: Long
}
```

生成排序等价于：

```kotlin
orderBy(table.snapshotTime.desc(), table.id.desc())
```

`groupBy` 暂不做注解化封装，因为它通常会改变 select 结果形状和返回类型；聚合查询继续直接使用 Jimmer 原生 DSL。

## Raw KSP fallback

不推荐优先使用 raw KSP；只有插件不可用时才手动接线：

```kotlin
dependencies {
    implementation("site.addzero:jimmer-low-query-annotations:<version>")
    ksp("site.addzero:jimmer-low-query-processor:<version>")
}

ksp {
    arg("jimmerLowQuery.generatedPackage", "com.example.generated.lowquery")
}
```

`processorBuddy.mustMap` 参数：

- `jimmerLowQuery.generatedPackage`：生成包名；留空时使用 `<实体包>.generated.lowquery`。

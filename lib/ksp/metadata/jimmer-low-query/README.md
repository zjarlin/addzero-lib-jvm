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
        select(table.fetchBy { allScalarFields() })
    }
}
```

实体入参建议用 Jimmer 草稿对象，只设置需要参与查询的字段；生成代码会用 `ImmutableObjects.isLoaded(entity, "字段名")` 判断字段是否已设置，未设置字段不会参与 where。

## 注解

- `@Eq(name = "", nullable = false)`：等值查询，生成动态谓词 ``table.xxx `eq?` param``。
- `@Like(name = "", nullable = false)`：模糊查询，生成动态谓词 ``table.xxx.`ilike?`(param, LikeMode.ANYWHERE)``。
- `@In(name = "", nullable = false)`：集合包含查询，生成动态谓词 ``table.xxx `valueIn?` params``；未指定 `name` 时默认用字段名简单复数形式。
- `@JimmerLowQuery(functionName = "query", clientFunctionName = "createLowQuery", visibility = PUBLIC, clientVisibility = PUBLIC, fetcher = ALL_SCALAR_FIELDS)`：可选，标在 `@Entity` 接口上，用于覆盖生成函数配置。
- `@JimmerLowQueryParam(name = "", operator = EQ, nullable = false)`：兼容旧写法；新代码优先使用字段语义注解。

支持的字段语义注解：`@Eq`、`@Ne`、`@Like`、`@StartsWith`、`@EndsWith`、`@Gt`、`@Ge`、`@Lt`、`@Le`、`@In`、`@NotIn`。

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

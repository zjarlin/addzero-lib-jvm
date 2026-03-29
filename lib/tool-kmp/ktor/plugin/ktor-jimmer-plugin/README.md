# ktor-jimmer-plugin

`ktor-jimmer-plugin` 只负责三件事：

- 按 `datasources.*` 配置创建 `DataSource`
- 为对应数据源创建默认 `KSqlClient`
- 暴露 Koin SPI，让消费方补充 schema / bootstrap / 兼容逻辑

它不再内置任何业务表结构，也不会自动执行 `autoddl`。

## What This Library Does Not Own

- 不提供 `schema-sqlite.sql` / `schema-postgres.sql`
- 不声明任何业务表
- 不决定是否做 `autoddl`
- 不做业务侧的历史数据修正

这些都属于消费方，例如 `apps/kcloud/server` 或具体业务插件自己的 `resources/sql/*`。

## Koin Wiring

推荐显式聚合，不要赌“第三方依赖里的 Koin 模块会不会自动被扫到”。

### 1. Root Koin Application

把库模块显式加到根 `@KoinApplication`：

```kotlin
@KoinApplication(
    modules = [
        site.addzero.kcloud.jimmer.di.JimmerKoinModule::class,
        MyServerJimmerKoinModule::class,
    ],
)
object MyServerKoinApplication
```

### 2. Pass `ApplicationConfig`

启动 Ktor/Koin 时，把 `ApplicationConfig` 放进 Koin property：

```kotlin
installKoin {
    withConfiguration<MyServerKoinApplication>()
    properties(
        mapOf(
            JIMMER_APPLICATION_CONFIG_PROPERTY to environment.config,
        ),
    )
}
```

如果你有桌面端内嵌 server 的 sqlite fallback，还需要：

```kotlin
System.setProperty(JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
```

对应常量：

- `site.addzero.kcloud.jimmer.di.JIMMER_APPLICATION_CONFIG_PROPERTY`
- `site.addzero.kcloud.jimmer.di.JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY`

## Consumer SPI

库提供的扩展点是 `JimmerDatasourceBootstrapSpi`。

它会在 `DataSource` 创建完成后、`KSqlClient` 创建前执行，适合做：

- 执行消费方自己的 schema SQL
- sqlite / postgres 的初始化差异
- 历史库兼容修正

如果多个插件都实现了这个 SPI，Koin 会把它们聚合成 `List<JimmerDatasourceBootstrapSpi>`，按 `order` 从小到大执行。

### 3. Register a Bootstrapper

最稳妥的方式是由消费方显式提供一个 Koin module：

```kotlin
@Module
class MyServerJimmerKoinModule {
    @Single
    fun provideDatasourceBootstrapper(): JimmerDatasourceBootstrapSpi {
        return MyServerSchemaBootstrapper()
    }
}
```

实现示例：

```kotlin
class MyServerSchemaBootstrapper : JimmerDatasourceBootstrapSpi {
    override fun onDataSourceReady(context: DatasourceBootstrapContext) {
        when {
            context.properties.driver.contains("sqlite", ignoreCase = true) -> {
                JimmerSqlScriptSupport.executeClasspathSql(
                    dataSource = context.dataSource,
                    resourcePath = "sql/schema-sqlite.sql",
                )
            }

            context.properties.driver.contains("postgres", ignoreCase = true) -> {
                JimmerSqlScriptSupport.executeClasspathSql(
                    dataSource = context.dataSource,
                    resourcePath = "sql/schema-postgres.sql",
                )
            }
        }
    }
}
```

辅助类：

- `site.addzero.kcloud.jimmer.support.JimmerSqlScriptSupport`

可用方法：

- `loadClasspathSql(resourcePath)`
- `executeStatements(dataSource, sql)`
- `executeClasspathSql(dataSource, resourcePath)`

## Resource Layout

把 SQL 放在消费方自己的资源目录：

```text
apps/my-server/src/jvmMain/resources/sql/schema-sqlite.sql
apps/my-server/src/jvmMain/resources/sql/schema-postgres.sql
```

如果你希望按业务插件拆分，也可以让每个插件各自提供一个 `JimmerDatasourceBootstrapSpi`，各自执行自己的 SQL 资源。库本身不会合并这些资源，也不会替你做中心注册表。

## Current KCloud Example

当前仓库里的落地方式：

- 库模块只保留 `JimmerKoinModule`、驱动 SPI、bootstrap SPI、SQL helper
- `apps/kcloud/server` 提供 `KCloudServerJimmerKoinModule`
- `apps/kcloud/server/src/jvmMain/resources/sql/` 持有实际 schema

也就是说，`autoddl` 和业务表结构现在完全归消费方所有，而不是 `ktor-jimmer-plugin`。

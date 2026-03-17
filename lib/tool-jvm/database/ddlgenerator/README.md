# ddlgenerator

- 作用：AutoDDL runtime façade，负责把 `LSI -> AutoDdlSchema`、`JDBC metadata -> AutoDdlSchema`、`schema diff -> SQL` 串起来。
- Maven 坐标：`site.addzero:ddlgenerator`
- 本地路径：`lib/tool-jvm/database/ddlgenerator`
- 分层：本模块只做运行时集成；`core`、`lsi-adaptor`、`jdbc-adaptor`、各数据库方言都已拆成独立模块。

## Minimal Usage

```kotlin
val fullSql = lsiClasses.toCompleteSchemaDdl(DatabaseType.POSTGRESQL)

val diffSql = lsiClasses.toDiffDdl(
    jdbcConfig = AutoDdlJdbcConfig(
        jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        jdbcUsername = "sa",
        jdbcPassword = ""
    ),
    databaseType = DatabaseType.H2
)
```

## Generated Settings

- `processorBuddy` 会生成：
  - `site.addzero.ddlgenerator.runtime.config.SettingContext`
  - `site.addzero.ddlgenerator.runtime.config.Settings`
- 这些配置只在 runtime 层转换成 `AutoDdlJdbcConfig` / `AutoDdlDiffOptions`。

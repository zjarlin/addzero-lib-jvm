# ddlgenerator-jdbc-adaptor

- 作用：把 JDBC metadata (`JdbcTableMetadata` / `JdbcIndexMetadata` / `ForeignKeyMetadata`) 转成 `AutoDdlSchema`。
- Maven 坐标：`site.addzero:ddlgenerator-jdbc-adaptor`
- 本地路径：`lib/tool-jvm/database/ddlgenerator-jdbc-adaptor`
- 约束：只做元数据归一化，不负责连库和 SQL 渲染。

```kotlin
val schema = JdbcAutoDdlSchemaAdapter.from(tables, foreignKeys, indexes)
```

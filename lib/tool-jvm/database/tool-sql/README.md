# tool-sql

- What it does: SQL string templates and builders for common patterns (e.g., data-density sort).
- Maven coordinate: `site.addzero:tool-sql`
- Local module path: `lib/tool-jvm/database/tool-sql`
- Constraints: Pure string building, no JDBC or ORM dependencies.

```kotlin
import site.addzero.util.db.sql.DensitySortSql

val sql = DensitySortSql.build(
    table = "big_table",
    columns = listOf("col1", "col2", "col3", "col4"),
    idColumn = "id"
)

val rawScript = DensitySortSql.FOUR_COLUMN_SCRIPT
```

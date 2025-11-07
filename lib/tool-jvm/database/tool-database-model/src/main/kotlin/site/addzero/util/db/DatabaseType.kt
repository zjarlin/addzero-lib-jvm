package site.addzero.util.db

/**
 * 数据库类型枚举
 */
enum class DatabaseType(
    val typeName: String
) {
    MYSQL("mysql"),
    POSTGRESQL("postgresql"),
    ORACLE("oracle"),
    SQLSERVER("sqlserver"),
    H2("h2"),
    SQLITE("sqlite");

}

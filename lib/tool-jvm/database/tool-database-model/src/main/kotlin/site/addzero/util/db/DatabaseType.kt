package site.addzero.util.db

/**
 * 关系型数据库类型枚举
 */
enum class DatabaseType(
    val desc: String
) {
    MYSQL("MySQL数据库"),
    POSTGRESQL("PostgreSQL数据库"),
    ORACLE("Oracle数据库"),
    SQLSERVER("SQL Server数据库"),
    H2("H2数据库"),
    SQLITE("SQLite数据库"),
    @Deprecated("已弃用，请使用DAMENG", replaceWith = ReplaceWith("site.addzero.util" +
            ".db.DatabaseType.DM"))
    DAMENG("达梦数据库"),
    DM("达梦数据库"),
    KINGBASE("人大金仓数据库"),
    GAUSSDB("华为高斯数据库"),
    OCEANBASE("蚂蚁金服OceanBase数据库"),
    POLARDB("阿里云PolarDB数据库"),
    TIDB("PingCAP TiDB数据库"),
    DB2("IBM DB2数据库"),
    SYBASE("Sybase数据库");

    val code: String
        get() = this.name.lowercase()

}

/**
 * 非关系型数据库类型枚举
 */
enum class NoSqlDatabaseType(
    val desc: String
) {
    MONGODB("MongoDB文档数据库"),
    REDIS("Redis内存数据库"),
    ELASTICSEARCH("Elasticsearch搜索引擎"),
    TDENGINE("TDengine时序数据库"),
    INFLUXDB("InfluxDB时序数据库"),
    CLICKHOUSE("ClickHouse列式数据库"),
    QUESTDB("QuestDB时序数据库");

    val code: String
        get() = this.name.lowercase()
}

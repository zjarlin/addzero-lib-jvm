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

    companion object {
        /**
         * 根据代码名称获取数据库类型（不区分大小写）
         */
        fun fromCode(code: String): DatabaseType? {
            return try {
                DatabaseType.valueOf(code.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 根据描述获取数据库类型（模糊匹配）
         */
        fun fromDesc(desc: String): DatabaseType? {
            return entries.find { it.desc.contains(desc, ignoreCase = true) }
        }

        /**
         * 根据名称获取数据库类型（不区分大小写）
         */
        fun fromName(name: String): DatabaseType? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }

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

    companion object {
        /**
         * 根据代码名称获取数据库类型（不区分大小写）
         */
        fun fromCode(code: String): NoSqlDatabaseType? {
            return try {
                NoSqlDatabaseType.valueOf(code.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 根据描述获取数据库类型（模糊匹配）
         */
        fun fromDesc(desc: String): NoSqlDatabaseType? {
            return entries.find { it.desc.contains(desc, ignoreCase = true) }
        }

        /**
         * 根据名称获取数据库类型（不区分大小写）
         */
        fun fromName(name: String): NoSqlDatabaseType? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}

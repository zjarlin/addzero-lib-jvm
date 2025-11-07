package site.addzero.util.db

/**
 * 数据库类型枚举
 */
enum class DatabaseType(
    val code: String,
    val desc: String
) {
    MYSQL("mysql", "MySQL数据库"),
    POSTGRESQL("postgresql", "PostgreSQL数据库"),
    ORACLE("oracle", "Oracle数据库"),
    SQLSERVER("sqlserver", "SQL Server数据库"),
    H2("h2", "H2数据库"),
    SQLITE("sqlite", "SQLite数据库"),
    DAMENG("dameng", "达梦数据库"),
    KINGBASE("kingbase", "人大金仓数据库"),
    GAUSSDB("gaussdb", "华为高斯数据库"),
    OCEANBASE("oceanbase", "蚂蚁金服OceanBase数据库"),
    POLARDB("polardb", "阿里云PolarDB数据库"),
    TIDB("tidb", "PingCAP TiDB数据库"),
    TDENGINE("tdengine", "TDengine时序数据库"),
    MONGODB("mongodb", "MongoDB文档数据库"),
    REDIS("redis", "Redis内存数据库"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch搜索引擎"),
    INFLUXDB("influxdb", "InfluxDB时序数据库"),
    CLICKHOUSE("clickhouse", "ClickHouse列式数据库"),
    DB2("db2", "IBM DB2数据库"),
    SYBASE("sybase", "Sybase数据库");
}
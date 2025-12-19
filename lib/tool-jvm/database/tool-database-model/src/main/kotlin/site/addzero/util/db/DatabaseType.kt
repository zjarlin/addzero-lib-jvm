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

        /**
         * 根据JDBC URL获取数据库类型
         * 支持关系型数据库的JDBC URL格式识别
         */
        fun fromUrl(url: String): DatabaseType? {
            return when {
                url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:mysqlc:") -> MYSQL
                url.startsWith("jdbc:postgresql:") -> POSTGRESQL
                url.startsWith("jdbc:oracle:") || url.startsWith("jdbc:oracle:thin:") -> ORACLE
                url.startsWith("jdbc:sqlserver:") -> SQLSERVER
                url.startsWith("jdbc:db2:") -> DB2
                url.startsWith("jdbc:h2:") -> H2
                url.startsWith("jdbc:sqlite:") -> SQLITE
                url.startsWith("jdbc:dm:") -> DM
                url.startsWith("jdbc:kingbase8:") || url.startsWith("jdbc:kingbase:") -> KINGBASE
                url.startsWith("jdbc:gaussdb:") -> GAUSSDB
                url.startsWith("jdbc:oceanbase:") -> OCEANBASE
                url.startsWith("jdbc:polardb:") -> POLARDB
                url.startsWith("jdbc:tidb:") -> TIDB
                url.startsWith("jdbc:sybase:") || url.startsWith("jdbc:jtds:") -> SYBASE
                else -> null
            }
        }

        /**
         * 根据JDBC URL获取驱动类名
         */
        fun getDriverClassName(url: String): String? {
            return when {
                url.startsWith("jdbc:postgresql:") -> "org.postgresql.Driver"
                url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:mysqlc:") -> "com.mysql.cj.jdbc.Driver"
                url.startsWith("jdbc:mariadb:") -> "org.mariadb.jdbc.Driver"
                url.startsWith("jdbc:sqlserver:") -> "com.microsoft.sqlserver.jdbc.SQLServerDriver"
                url.startsWith("jdbc:oracle:") || url.startsWith("jdbc:oracle:thin:") -> "oracle.jdbc.OracleDriver"
                url.startsWith("jdbc:db2:") -> "com.ibm.db2.jcc.DB2Driver"
                url.startsWith("jdbc:h2:") -> "org.h2.Driver"
                url.startsWith("jdbc:sqlite:") -> "org.sqlite.JDBC"
                url.startsWith("jdbc:dm:") -> "dm.jdbc.driver.DmDriver"
                url.startsWith("jdbc:kingbase8:") || url.startsWith("jdbc:kingbase:") -> "com.kingbase8.Driver"
                url.startsWith("jdbc:gaussdb:") -> "org.opengauss.Driver"
                url.startsWith("jdbc:oceanbase:") -> "com.oceanbase.jdbc.Driver"
                url.startsWith("jdbc:polardb:") -> "com.mysql.cj.jdbc.Driver"
                url.startsWith("jdbc:tidb:") -> "com.mysql.cj.jdbc.Driver"
                url.startsWith("jdbc:sybase:") -> "com.sybase.jdbc4.jdbc.SybDriver"
                url.startsWith("jdbc:jtds:") -> "net.sourceforge.jtds.jdbc.Driver"
                else -> null
            }
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

        /**
         * 根据连接URL获取非关系型数据库类型
         * 支持常见NoSQL数据库的连接URL格式识别
         */
        fun fromUrl(url: String): NoSqlDatabaseType? {
            return when {
                url.startsWith("mongodb:") || url.startsWith("mongodb+srv:") -> MONGODB
                url.startsWith("redis:") || url.startsWith("rediss:") -> REDIS
                url.startsWith("http:") && url.contains("elasticsearch") -> ELASTICSEARCH
                url.startsWith("https:") && url.contains("elasticsearch") -> ELASTICSEARCH
                url.startsWith("jdbc:tdengine:") -> TDENGINE
                url.startsWith("jdbc:influxdb:") || url.startsWith("http:") && url.contains("influxdb") -> INFLUXDB
                url.startsWith("jdbc:clickhouse:") || url.startsWith("clickhouse:") -> CLICKHOUSE
                url.startsWith("jdbc:questdb:") || url.startsWith("http:") && url.contains("questdb") -> QUESTDB
                else -> null
            }
        }

        /**
         * 根据URL获取对应的连接客户端类名（如果适用）
         */
        fun getClientClassName(url: String): String? {
            return when {
                url.startsWith("mongodb:") || url.startsWith("mongodb+srv:") -> "com.mongodb.client.MongoClient"
                url.startsWith("redis:") || url.startsWith("rediss:") -> "redis.clients.jedis.Jedis"
                url.startsWith("http:") && url.contains("elasticsearch") -> "org.elasticsearch.client.RestHighLevelClient"
                url.startsWith("https:") && url.contains("elasticsearch") -> "org.elasticsearch.client.RestHighLevelClient"
                url.startsWith("jdbc:tdengine:") -> "com.taosdata.jdbc.TSDBDriver"
                url.startsWith("jdbc:influxdb:") -> "org.influxdb.InfluxDB"
                url.startsWith("http:") && url.contains("influxdb") -> "org.influxdb.InfluxDB"
                url.startsWith("jdbc:clickhouse:") -> "ru.yandex.clickhouse.ClickHouseDriver"
                url.startsWith("clickhouse:") -> "ru.yandex.clickhouse.ClickHouseDataSource"
                url.startsWith("jdbc:questdb:") -> "io.questdb.client.Driver"
                url.startsWith("http:") && url.contains("questdb") -> "io.questdb.client.Sender"
                else -> null
            }
        }
    }
}

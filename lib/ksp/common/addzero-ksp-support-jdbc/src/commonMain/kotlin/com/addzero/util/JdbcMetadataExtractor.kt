package com.addzero.util

import com.addzero.entity.JdbcTableMetadata
import com.addzero.util.DatabaseMetadataUtil.getTableMetaData
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*


/**
 * JDBC元数据抽取工具类（单例静态工具类）
 *
 * 功能：独立的数据库元数据抽取工具，负责连接数据库并提取表结构信息
 * 与KSP处理器解耦，可以独立使用
 * 采用单例模式，所有方法均为静态方法
 */
object JdbcMetadataExtractor {

    /**
     * JDBC连接配置
     */
    data class JdbcConfig(
        val jdbcUrl: String = "jdbc:postgresql://localhost:5432/postgres",
        val jdbcUsername: String = "postgres",
        val jdbcPassword: String = "postgres",
        val jdbcSchema: String = "public",
        val jdbcDriver: String = "org.postgresql.Driver",
        val includeTables: List<String>? = null,
        val excludeTables: List<String>? = null,
        val excludeColumns: List<String> = DEFAULT_EXCLUDE_COLUMNS
    )

    val DEFAULT_EXCLUDE_COLUMNS = listOf(
        "create_time",
        "update_time",
        "create_by",
        "update_by",
        "created_at",
        "updated_at",
        "created_by",
        "updated_by",
        "deleted",
        "deleted_at",
        "is_deleted",
        "tenant_id",
        "delflag"
    )

    /**
     * 从KSP选项创建配置
     */
    fun fromKspOptions(options: Map<String, String>): JdbcConfig {
        return JdbcConfig(
            jdbcUrl = options["jdbcUrl"] ?: "jdbc:postgresql://localhost:5432/postgres",
            jdbcUsername = options["jdbcUsername"] ?: "postgres",
            jdbcPassword = options["jdbcPassword"] ?: "postgres",
            jdbcSchema = options["jdbcSchema"] ?: "public",
            jdbcDriver = options["jdbcDriver"] ?: "org.postgresql.Driver",
            includeTables = options["includeTables"]?.split(",")?.map { it.trim() },
            excludeTables = options["excludeTables"]?.split(",")?.map { it.trim() },
            excludeColumns = options["excludeColumns"]?.split(",")?.map { it.trim() }
                ?: DEFAULT_EXCLUDE_COLUMNS
        )
    }


    fun initAndGetJdbcMetaDataTables(kspOpntions: Map<String, String>): List<JdbcTableMetadata> {
        println("开始JDBC元数据处理...")

        // 从KSP选项创建配置
        val config = fromKspOptions(kspOpntions)

        // 测试数据库连接
        if (!testConnection(config)) {
            println("数据库连接失败，请检查配置")
            return emptyList()
        }

        // 尝试从数据库提取元数据
        val tables = try {
            extractDatabaseMetadata(config)
        } catch (e: Exception) {
            println("⚠️ 无法连接到数据库或提取元数据: ${e.message}")
            println("跳过JDBC元数据生成过程")
            when (e) {
                is ClassNotFoundException -> println("找不到JDBC驱动")
                is SQLException -> println("SQL错误: ${e.message}, 错误代码: ${e.errorCode}, SQL状态: ${e.sqlState}")
                else -> e.printStackTrace()
            }
            // 返回空列表表示没有表可处理
            return emptyList()
        }

        if (tables.isEmpty()) {
            println("没有找到符合条件的表, 跳过生成过程")
            return emptyList()
        }
        return tables
    }


    /**
     * 从数据库中提取元数据
     * @param config JDBC配置
     * @throws java.lang.ClassNotFoundException 如果找不到JDBC驱动
     * @throws java.sql.SQLException 如果数据库连接或查询失败
     */
    fun extractDatabaseMetadata(config: JdbcConfig): List<JdbcTableMetadata> {
        var connection: Connection? = null

        val tables = try {
            // 加载驱动
            Class.forName(config.jdbcDriver)

            // 建立连接
            connection = createConnection(config)
            println("数据库连接成功")

            val tables = getTableMetaData(connection, config.jdbcSchema, config.includeTables, config.excludeTables)
            println("成功从数据库读取了 ${tables.size} 个表的元数据")
            tables
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                println("关闭数据库连接时发生错误: ${e.message}")
            }
        }

        return tables
    }


    /**
     * 创建数据库连接
     */
    private fun createConnection(config: JdbcConfig): Connection {
        val props = Properties()
        props.setProperty("user", config.jdbcUsername)
        props.setProperty("password", config.jdbcPassword)

        // 设置连接超时 (5秒)
        props.setProperty("connectTimeout", "5")

        println("正在连接数据库: ${config.jdbcUrl}")
        return DriverManager.getConnection(config.jdbcUrl, props)
    }

    /**
     * 判断是否应该排除某列
     */
    private fun shouldExcludeColumn(columnName: String, config: JdbcConfig): Boolean {
        // 检查列名是否在排除列表中 (不区分大小写)
        return config.excludeColumns.any {
            it.equals(columnName, ignoreCase = true)
        }
    }





    /**
     * 测试数据库连接
     * @param config JDBC配置
     */
    private fun testConnection(config: JdbcConfig): Boolean {
        return try {
            Class.forName(config.jdbcDriver)
            val connection = createConnection(config)
            connection.close()
            println("数据库连接测试成功")
            true
        } catch (e: Exception) {
            println("数据库连接测试失败: ${e.message}")
            when (e) {
                is ClassNotFoundException -> println("找不到JDBC驱动: ${config.jdbcDriver}")
                is SQLException -> println("SQL错误: ${e.message}, 错误代码: ${e.errorCode}, SQL状态: ${e.sqlState}")
                else -> e.printStackTrace()
            }
            false
        }
    }
}

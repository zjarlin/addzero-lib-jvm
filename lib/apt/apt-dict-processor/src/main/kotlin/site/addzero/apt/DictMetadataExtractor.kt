package site.addzero.apt

import site.addzero.apt.config.DictProcessorConfig
import site.addzero.apt.config.DictProcessorSettings
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

/**
 * 字典元数据抽取器
 *
 * 从数据库中抽取字典表和字典项表的元数据
 */
class DictMetadataExtractor(
    private val messager: Messager
) {
    private val config: DictProcessorConfig = DictProcessorSettings.getSettings()
    /**
     * 字典项数据类
     */
    data class DictItem(
        val code: String,
        val desc: String
    )

    /**
     * 字典元数据
     */
    data class DictMetadata(
        val dictCode: String,
        val dictName: String,
        val items: List<DictItem>
    )

    /**
     * 从数据库提取字典元数据
     */
    fun extractDictMetadata(): List<DictMetadata> {
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] ========== 开始字典元数据抽取 ==========")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] JDBC 驱动: ${config.jdbcDriver}")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] JDBC URL: ${config.jdbcUrl}")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 数据库用户名: ${config.jdbcUsername}")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 密码长度: ${config.jdbcPassword.length} 字符")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 密码首字符: ${config.jdbcPassword.firstOrNull()}")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 字典表: ${config.dictTableName}")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 字典项表: ${config.dictItemTableName}")
        
        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 正在加载 JDBC 驱动...")
            Class.forName(config.jdbcDriver)
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] JDBC 驱动加载成功")
        } catch (e: ClassNotFoundException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[ERROR] 无法加载 JDBC 驱动: ${e.message}")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 请确保 mysql-connector-java 在 annotationProcessorPaths 中")
            throw e
        }

        val props = Properties().apply {
            setProperty("user", config.jdbcUsername)
            setProperty("password", config.jdbcPassword)
        }
        
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] Properties 配置完成，准备连接...")
        messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 当前时间: ${System.currentTimeMillis()}")

        return try {
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 调用 DriverManager.getConnection()...")
            val startTime = System.currentTimeMillis()
            
            DriverManager.getConnection(config.jdbcUrl, props).use { connection ->
                val connectTime = System.currentTimeMillis() - startTime
                messager.printMessage(Diagnostic.Kind.NOTE, "[INFO] 数据库连接成功 (耗时: ${connectTime}ms)")
                messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 数据库版本: ${connection.metaData.databaseProductVersion}")
                messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 数据库产品: ${connection.metaData.databaseProductName}")
                messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 连接URL: ${connection.metaData.url}")
                messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 连接用户: ${connection.metaData.userName}")

                val sql = buildQuery()
                messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 执行查询: $sql")

                connection.createStatement().use { statement ->
                    statement.executeQuery(sql).use { resultSet ->
                        val dictMap = linkedMapOf<String, MutableList<DictItem>>()
                        val dictNameMap = linkedMapOf<String, String>()

                        while (resultSet.next()) {
                            val dictCode = resultSet.getString("dict_code")
                            val dictName = resultSet.getString("dict_name")
                            val itemCode = resultSet.getString("item_code")
                            val itemDesc = resultSet.getString("item_desc")

                            dictNameMap[dictCode] = dictName

                            if (itemCode != null && itemDesc != null) {
                                dictMap.getOrPut(dictCode) { mutableListOf() }
                                    .add(DictItem(itemCode, itemDesc))
                            }
                        }

                        if (dictMap.isEmpty()) {
                            messager.printMessage(
                                Diagnostic.Kind.WARNING,
                                "[WARNING] 未找到任何字典数据，请检查数据库表 ${config.dictTableName} 和 ${config.dictItemTableName} 是否存在数据"
                            )
                            return@use emptyList()
                        }

                        messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "[INFO] 从数据库读取到 ${dictMap.size} 个字典"
                        )

                        dictMap
                            .filter { it.value.isNotEmpty() }
                            .map { (dictCode, items) ->
                                DictMetadata(
                                    dictCode = dictCode,
                                    dictName = dictNameMap[dictCode] ?: "",
                                    items = items
                                )
                            }
                    }
                }
            }
        } catch (e: SQLException) {
            messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] ========== 数据库连接失败 ==========")
            messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 异常类型: ${e.javaClass.name}")
            messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 错误消息: ${e.message}")
            messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] SQL状态: ${e.sqlState}")
            messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 错误码: ${e.errorCode}")
            
            e.cause?.let { cause ->
                messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 根本原因: ${cause.javaClass.name}")
                messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 根本原因消息: ${cause.message}")
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] ========== 连接参数回顾 ==========")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] JDBC URL: ${config.jdbcUrl}")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 用户名: ${config.jdbcUsername}")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 密码已设置: ${config.jdbcPassword.isNotEmpty()}")
            
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] ========== 可能的原因 ==========")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 1. 数据库服务未启动或无法访问")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 2. 网络连接问题（防火墙/网络隔离）")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 3. 用户名或密码错误")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 4. 数据库不存在或权限不足")
            messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 5. URL参数配置错误")
            
            throw e
        } catch (e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[ERROR] ========== 未预期的异常 ==========")
            messager.printMessage(Diagnostic.Kind.ERROR, "[ERROR] 异常类型: ${e.javaClass.name}")
            messager.printMessage(Diagnostic.Kind.ERROR, "[ERROR] 错误消息: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * 构建 SQL 查询语句
     */
    private fun buildQuery(): String {
        return """
            SELECT 
                d.${config.dictIdColumn} as dict_id,
                d.${config.dictCodeColumn} as dict_code,
                d.${config.dictNameColumn} as dict_name,
                i.${config.dictItemCodeColumn} as item_code,
                i.${config.dictItemNameColumn} as item_desc
            FROM 
                ${config.dictTableName} d
            LEFT JOIN 
                ${config.dictItemTableName} i ON d.${config.dictIdColumn} = i.${config.dictItemForeignKeyColumn}
            ORDER BY 
                d.${config.dictCodeColumn}, i.${config.dictItemCodeColumn}
        """.trimIndent()
    }
}

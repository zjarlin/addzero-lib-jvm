package site.addzero.apt

import site.addzero.apt.config.DictProcessorConfig
import site.addzero.apt.config.DictProcessorSettings
import java.sql.Connection
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
    private val messager: Messager,
    private val config: DictProcessorConfig
) {
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
        // 加载 JDBC 驱动
        Class.forName(config.jdbcDriver)

        messager.printMessage(Diagnostic.Kind.NOTE, "[INFO] 正在连接数据库: ${config.jdbcUrl}")

        val props = Properties().apply {
            setProperty("user", config.jdbcUsername)
            setProperty("password", config.jdbcPassword)
            setProperty("connectTimeout", "5")
        }

        return DriverManager.getConnection(config.jdbcUrl, props).use { connection ->
            messager.printMessage(Diagnostic.Kind.NOTE, "[INFO] 数据库连接成功")

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

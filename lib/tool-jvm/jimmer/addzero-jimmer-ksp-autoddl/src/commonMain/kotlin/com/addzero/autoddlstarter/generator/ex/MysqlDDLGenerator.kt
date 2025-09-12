package com.addzero.autoddlstarter.generator.ex

import com.addzero.autoddlstarter.context.AutoDDLSettings
import com.addzero.autoddlstarter.context.DDLContext
import com.addzero.autoddlstarter.generator.DatabaseDDLGenerator
import com.addzero.util.str.makeSurroundWith
import com.addzero.util.str.toUnderLineCase


class MysqlDDLGenerator : DatabaseDDLGenerator() {
    override val defaultStringType: String
        get() = "VARCHAR(255)"
    override val selfMappingTypeTable: Map<String, String>
        get() = mapOf(
            "String" to "VARCHAR(255)",
            "Int" to "INT",
            "Long" to "BIGINT",
            "Double" to "DOUBLE",
            "Float" to "FLOAT",
            "Boolean" to "TINYINT(1)",
            "LocalDateTime" to "DATETIME",
            "LocalDate" to "DATE",
            "LocalTime" to "TIME",
            "BigDecimal" to "DECIMAL",
            "UUID" to "CHAR(36)"
        )

    override fun generateCreateTableDDL(ddlContext: DDLContext): String {
        val tableEnglishName = ddlContext.tableEnglishName
        val tableChineseName = ddlContext.tableChineseName
        val dto = ddlContext.dto
        val createTableSQL = """
    create table if not exists `$tableEnglishName` (
        ${
            dto
                .distinctBy { it.colName }
                .joinToString(System.lineSeparator()) {
                    val colLength = it.colLength
                    """
                       `${it.colName.toUnderLineCase()}` ${it.colType}    $colLength    comment '${it.colComment}' ,
                """.trimIndent()
                }
        }
        primary key (`id`)
    ) engine=innodb default charset=utf8mb4
     comment = '${tableChineseName}'; 
""".trimIndent()
        return createTableSQL
    }


    override fun generateAddColDDL(ddlContext: DDLContext): String {
        val (tableChineseName, tableEnglishName, databaseType, databaseName, dto) = ddlContext
        val dmls = dto
            .joinToString(System.lineSeparator()) {
                // 如果 databaseName 不为空，则拼接成 databaseName.tableEnglishName
                val tableRef = if (databaseName.isEmpty()) {
                    tableEnglishName.makeSurroundWith("`")
                } else {
                    "`$databaseName`.`$tableEnglishName`"
                }
                // 生成 ALTER 语句以及字段注释
                val toUnderlineCase = it.colName.toUnderLineCase()
                """
            alter table $tableRef add column `$toUnderlineCase` ${it.colType} ${it.colLength}  comment '${it.colComment}';
        """.trimIndent()
            }
        return dmls
    }


}

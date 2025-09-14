package site.addzero.autoddlstarter.generator.ex

import site.addzero.autoddlstarter.context.AutoDDLSettings
import site.addzero.autoddlstarter.context.DDLContext
import site.addzero.autoddlstarter.generator.DatabaseDDLGenerator
import site.addzero.util.str.JlStrUtil
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.str.toUnderLineCase

class H2SQLDDLGenerator : DatabaseDDLGenerator() {
    override val defaultStringType: String
        get() = "VARCHAR"
    override val selfMappingTypeTable: Map<String, String>
        get() = mapOf(
            "String" to "VARCHAR",
            "Int" to "INT",
            "Long" to "BIGINT",
            "Double" to "DOUBLE",
            "Float" to "REAL",
            "Boolean" to "BOOLEAN",
            "LocalDateTime" to "TIMESTAMP",
            "LocalDate" to "DATE",
            "LocalTime" to "TIME",
            "BigDecimal" to "DECIMAL",
            "UUID" to "UUID"
        )

    override fun generateCreateTableDDL(ddlContext: DDLContext): String {
        val tableEnglishName = ddlContext.tableEnglishName
        val tableChineseName = ddlContext.tableChineseName
        val dto = ddlContext.dto

        var cols = """        ${
            dto.joinToString(System.lineSeparator()) {
                """
                    `${it.colName}` ${it.colType}  ${it.colLength},
                """.trimIndent()
            }
        }
"""

        cols = JlStrUtil.removeLastCharOccurrence(cols, ',')

        val settings = AutoDDLSettings.settings
        val createTableSQL = """
    create table if not exists "$tableEnglishName" (
$cols       
    );
    comment on table "$tableEnglishName" is '$tableChineseName';
    """.trimIndent()

        // 添加字段注释
        val comments = dto
            .distinctBy { it.colName }
//            .filter { filterBaseEneity(it) }
            .joinToString(System.lineSeparator()) {
                """
                comment on column "$tableEnglishName".`${it.colName}` is '${it.colComment}';
                """.trimIndent()
            }


        val join = JlStrUtil.join(System.lineSeparator(), createTableSQL, comments)
        return join
    }

    override fun generateAddColDDL(ddlContext: DDLContext): String {
        val (tableChineseName, tableEnglishName, databaseType, databaseName, dto) = ddlContext
        val dmls = dto.joinToString(System.lineSeparator()) {

            // 如果 databaseName 不为空，则拼接成 databaseName.tableEnglishName
            val tableRef = if (databaseName.isNullOrEmpty()) {
                tableEnglishName.makeSurroundWith("\"")
            } else {
                "\"$databaseName\".\"$tableEnglishName\""
            }
            // 生成 ALTER 语句以及字段注释

            val upperCaseColName = it.colName.toUnderLineCase()
            """
            alter table $tableRef add column `$upperCaseColName` ${it.colType}${it.colLength};
            comment on column $tableRef.`$upperCaseColName` is '${it.colComment}';
        """.trimIndent()
        }

        return dmls
    }

}

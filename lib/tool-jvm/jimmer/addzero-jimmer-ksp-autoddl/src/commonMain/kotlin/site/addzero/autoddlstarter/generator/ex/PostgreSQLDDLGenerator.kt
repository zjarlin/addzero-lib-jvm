package site.addzero.autoddlstarter.generator.ex

import site.addzero.autoddlstarter.generator.DatabaseDDLGenerator
import site.addzero.autoddlstarter.context.DDLContext
import site.addzero.autoddlstarter.context.AutoDDLSettings
import site.addzero.util.str.JlStrUtil
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.str.toUnderLineCase

class PostgreSQLDDLGenerator : DatabaseDDLGenerator() {
    override val defaultStringType: String
        get() ="TEXT"
override val selfMappingTypeTable: Map<String, String>
    get() = mapOf(
        "String" to "TEXT",
        "Int" to "INTEGER",
        "Long" to "BIGINT",
        "Double" to "DOUBLE PRECISION",
        "Float" to "REAL",
        "Boolean" to "BOOLEAN",
        "LocalDateTime" to "TIMESTAMP",
        "LocalDate" to "DATE",
        "LocalTime" to "TIME",
        "BigDecimal" to "NUMERIC",
        "UUID" to "UUID"
    )

    override fun generateCreateTableDDL(ddlContext: DDLContext): String {
        val tableEnglishName = ddlContext.tableEnglishName
        val tableChineseName = ddlContext.tableChineseName
        val dto = ddlContext.dto

        var cols = """        ${
            dto.joinToString(System.lineSeparator()) {
                """
                    ${it.colName} ${it.colType}  ${it.colLength},
                """.trimIndent()
            }
        }
"""

        cols = JlStrUtil.removeLastCharOccurrence(cols, ',')


        val colsComments = """        ${
            dto
                .distinctBy { it.colName }

//                .filter { it.colName ignoreCaseNotIn listOf(id, createBy, updateBy, createTime, updateTime) }

//                .filter { it.colName !in listOf(id, createBy, updateBy, createTime, updateTime) }
                .joinToString(System.lineSeparator()) {
                    """
 comment on column $tableEnglishName.${it.colName} is '${it.colComment}'; 
                """.trimIndent()
                }
        }
"""

        val createTableSQL = """
    create table if not exists "$tableEnglishName" (
$cols       
    );
   ALTER TABLE "$tableEnglishName" ADD PRIMARY KEY (id); 
    comment on table "$tableEnglishName" is '$tableChineseName';
    $colsComments
    
    
""".trimIndent()

        return createTableSQL
    }

    override fun generateAddColDDL(ddlContext: DDLContext): String {
        val (tableChineseName, tableEnglishName, databaseType, databaseName, dto) = ddlContext
        val dmls = dto.joinToString(System.lineSeparator()) {

            // 如果 databaseName 不为空，则拼接成 databaseName.tableEnglishName
            val tableRef = if (databaseName.isNullOrEmpty()) {
              tableEnglishName .makeSurroundWith("\"")
            } else {
                "\"$databaseName\".\"$tableEnglishName\""
            }
            // 生成 ALTER 语句以及字段注释
            val upperCaseColName = it.colName.toUnderLineCase()
            """
            alter table $tableRef add column "$upperCaseColName" ${it.colType}${it.colLength}; comment on column $tableRef."$upperCaseColName" is '${it.colComment}';
        """.trimIndent()
        }

        return dmls
    }


}

package site.addzero.autoddlstarter.generator.ex

import site.addzero.autoddlstarter.generator.DatabaseDDLGenerator
import site.addzero.autoddlstarter.context.DDLContext
import site.addzero.autoddlstarter.context.AutoDDLSettings
import site.addzero.util.str.JlStrUtil
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.str.toUnderLineCase

class OracleDDLGenerator : DatabaseDDLGenerator() {
    override val defaultStringType: String
        get() = "VARCHAR2"
override val selfMappingTypeTable: Map<String, String>
    get() = mapOf(
        "String" to "VARCHAR2",
        "Int" to "NUMBER(10)",
        "Long" to "NUMBER(19)",
        "Double" to "BINARY_DOUBLE",
        "Float" to "BINARY_FLOAT",
        "Boolean" to "NUMBER(1)",
        "LocalDateTime" to "TIMESTAMP",
        "LocalDate" to "DATE",
        "LocalTime" to "DATE",
        "BigDecimal" to "NUMBER",
        "UUID" to "RAW(16)"
    )

    override fun generateCreateTableDDL(ddlContext: DDLContext): String {
        var (tableChineseName, tableEnglishName, databaseType, databaseName, dto) = ddlContext
        val uppercase = tableEnglishName.uppercase()
        tableEnglishName = uppercase

        val tableRef = if (databaseName.isEmpty()) {
            uppercase.makeSurroundWith("\"")
        } else {
            "\"$databaseName\".\"$uppercase\""
        }
        val settings = AutoDDLSettings.settings


        val idType = settings.idType
        val createTableSQL = """
    CREATE TABLE $tableRef (
        ${
            dto
                .distinctBy { it.colName }

//                .filter { it.colName ignoreCaseNotIn listOf(id, createBy, updateBy, createTime, updateTime) }

            .joinToString(System.lineSeparator()) {
                """
                    "${it.colName.uppercase()}" ${it.colType} ${it.colLength?.let { length -> "($length)" }} NOT NULL
                """.trimIndent()
            }
        },
        PRIMARY KEY ("ID")
    );

    COMMENT ON TABLE "$tableEnglishName" IS '$tableChineseName';
    """.trimIndent()

        // 添加字段注释
        val comments = dto.joinToString(System.lineSeparator()) {
            """
            COMMENT ON COLUMN $tableRef."${it.colName.uppercase()}" IS '${it.colComment}';
            """.trimIndent()
        }

     val join =   JlStrUtil.join(System.lineSeparator(), createTableSQL,comments)

        return join
    }

    override fun generateAddColDDL(ddlContext: DDLContext): String {
        val (tableChineseName, tableEnglishName, databaseType, databaseName, dto) = ddlContext


        val dmls = dto
        .joinToString(System.lineSeparator()) {

            val tableRef = if (databaseName.isNullOrEmpty()) {
                tableEnglishName.uppercase().makeSurroundWith("\"")
            } else {
                "\"$databaseName\".\"${tableEnglishName.uppercase()}\""
            }

            // 生成 ALTER 语句以及字段属性

            val upperCaseColName =  it.colName.toUnderLineCase().uppercase()
            val addColumnDDL = """
            ALTER TABLE $tableRef ADD ("$upperCaseColName" ${it.colType}(${it.colLength})); 
            """.trimIndent()

            val commentDDL = """
            COMMENT ON COLUMN $tableRef."$upperCaseColName" IS '${it.colComment}';
            """.trimIndent()

            "$addColumnDDL\n$commentDDL"
        }

        return dmls
    }

}

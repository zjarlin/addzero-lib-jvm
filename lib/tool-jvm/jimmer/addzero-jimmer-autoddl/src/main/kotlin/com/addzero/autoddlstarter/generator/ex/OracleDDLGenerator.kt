package com.addzero.autoddlstarter.generator.ex

import cn.hutool.core.util.StrUtil
import com.addzero.autoddlstarter.generator.DatabaseDDLGenerator
import com.addzero.autoddlstarter.generator.IDatabaseGenerator.Companion.fieldMappings
import com.addzero.autoddlstarter.generator.entity.JavaFieldMetaInfo
import com.addzero.autoddlstarter.generator.filterBaseEneity
import com.addzero.autoddlstarter.context.DDLContext
import com.addzero.autoddlstarter.context.SettingContext
import com.addzero.autoddlstarter.util.JlStrUtil

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
        tableEnglishName = tableEnglishName.uppercase()

        val tableRef = if (databaseName.isNullOrBlank()) {
            JlStrUtil.makeSurroundWith(tableEnglishName.uppercase(), "\"")
        } else {
            "\"$databaseName\".\"${tableEnglishName.uppercase()}\""
        }
        val settings = SettingContext.settings

        val id = settings.id
        val createBy = settings.createBy
        val updateBy = settings.updateBy
        val createTime = settings.createTime
        val updateTime = settings.updateTime

        val idType = settings.idType
        val createTableSQL = """
    CREATE TABLE $tableRef (
        "$id" $idType NOT NULL,
        "$createBy" $idType NOT NULL,
        "$updateBy" $idType NULL,
        "$createTime" TIMESTAMP,
        "$updateTime" TIMESTAMP,
        ${
            dto
                .distinctBy { it.colName }

                .filter { filterBaseEneity(it) }
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
        val base = """
            COMMENT ON COLUMN $tableRef.$id IS '主键';
            COMMENT ON COLUMN $tableRef.$createBy IS '创建者';
            COMMENT ON COLUMN $tableRef.$createTime IS '创建时间';
            COMMENT ON COLUMN $tableRef.$updateBy IS '更新者';
            COMMENT ON COLUMN $tableRef.$updateTime IS '更新时间'; 
"""


        val join = StrUtil.join(System.lineSeparator(), createTableSQL, base, comments)

        return join
    }

    override fun generateAddColDDL(ddlContext: DDLContext): String {
        val (tableChineseName, tableEnglishName, databaseType, databaseName, dto) = ddlContext


        val dmls = dto
        .joinToString(System.lineSeparator()) {

            val tableRef = if (databaseName.isNullOrBlank()) {
                JlStrUtil.makeSurroundWith(tableEnglishName.uppercase(), "\"")
            } else {
                "\"$databaseName\".\"${tableEnglishName.uppercase()}\""
            }

            // 生成 ALTER 语句以及字段属性
            val upperCaseColName = StrUtil.toUnderlineCase(it.colName).uppercase()
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

    override fun mapTypeByMysqlType(mysqlType: String): String {
        return fieldMappings.find { it.mysqlType.equals(mysqlType, ignoreCase = true) }?.oracleType!!
    }

    override fun mapTypeByJavaType(javaFieldMetaInfo: JavaFieldMetaInfo): String {
        return fieldMappings.find { it.predi.test(javaFieldMetaInfo) }?.oracleType!!
    }
}

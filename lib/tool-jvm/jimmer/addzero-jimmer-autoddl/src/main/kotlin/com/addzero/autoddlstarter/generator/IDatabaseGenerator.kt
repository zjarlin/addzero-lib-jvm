package com.addzero.autoddlstarter.generator

import cn.hutool.core.util.StrUtil
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isBigDecimalType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isBooleanType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isCharType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isDateTimeType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isDateType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isDoubleType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isIntType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isLongType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isStringType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isTextType
import com.addzero.autoddlstarter.generator.FieldPredicateUtil.isTimeType
import com.addzero.autoddlstarter.generator.entity.FieldMapping
import com.addzero.autoddlstarter.generator.entity.JavaFieldMetaInfo
import com.addzero.autoddlstarter.context.DDlRangeContext
import com.addzero.autoddlstarter.context.SettingContext
import com.addzero.autoddlstarter.generator.consts.DbType.DM
import com.addzero.autoddlstarter.generator.consts.DbType.H2
import com.addzero.autoddlstarter.generator.consts.DbType.MYSQL
import com.addzero.autoddlstarter.generator.consts.DbType.ORACLE
import com.addzero.autoddlstarter.generator.consts.DbType.POSTGRESQL
import com.addzero.autoddlstarter.generator.ex.DMSQLDDLGenerator
import com.addzero.autoddlstarter.generator.ex.H2SQLDDLGenerator
import com.addzero.autoddlstarter.generator.ex.MysqlDDLGenerator
import com.addzero.autoddlstarter.generator.ex.OracleDDLGenerator
import com.addzero.autoddlstarter.generator.ex.PostgreSQLDDLGenerator
import com.addzero.autoddlstarter.util.containsAny
import com.addzero.autoddlstarter.util.ignoreCaseIn
import com.addzero.autoddlstarter.util.ignoreCaseLike
import com.addzero.autoddlstarter.util.toLowCamelCase
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*



fun filterBaseEneity(dDlRangeContext: DDlRangeContext): Boolean {
    val colName = dDlRangeContext.colName

    return filterBaseEntity(colName)
}

 fun filterBaseEntity(colName: String): Boolean {
    val settings = SettingContext.settings
    val id = settings.id
    val createBy = settings.createBy
    val updateBy = settings.updateBy
    val createTime = settings.createTime
    val updateTime = settings.updateTime

    if (colName.isNullOrEmpty()) {
        return false
    }
    val arrayOf = arrayOf(id, createBy, updateBy, createTime, updateTime)
    val arrayOf1 = arrayOf.map { it.toLowCamelCase() }.toTypedArray()
    arrayOf(id, createBy, updateBy, createTime, updateTime)
    val containsAny = StrUtil.containsAny(colName, *arrayOf)
    val containsAny1 = StrUtil.containsAny(colName, *arrayOf1)
    val b = !(containsAny|| containsAny1)
    return b
}

interface IDatabaseGenerator {


    /**
     * 依据mysql类型推导出各种sql类型
     * @param [mysqlType]
     * @return [String]
     */
    fun mapTypeByMysqlType(mysqlType: String): String

    /**
     * 依据java类型推导出各种sql类型
     * @param [javaFieldMetaInfo]
     * @return [String]
     */
    fun mapTypeByJavaType(javaFieldMetaInfo: JavaFieldMetaInfo): String


    companion object {

        fun getLength(javaFieldMetaInfo: JavaFieldMetaInfo): String {
            return fieldMappings.find { it.predi.test(javaFieldMetaInfo) }?.length!!
        }

        fun getDatabaseDDLGenerator(dbType: String): DatabaseDDLGenerator {
            val generator = databaseType[dbType]
            return generator!!
        }

        fun javaType2RefType(javaType: String): String? {
            val s = when {
                javaType ignoreCaseLike "int" -> Int::class.java.name
                javaType ignoreCaseLike "long" -> Long::class.java.name
                javaType ignoreCaseLike "double" -> Double::class.java.name
                javaType ignoreCaseLike "float" -> Float::class.java.name
                javaType ignoreCaseLike "boolean" -> Boolean::class.java.name
                javaType ignoreCaseLike "string" -> String::class.java.name
                javaType ignoreCaseLike "date" -> Date::class.java.name
                javaType ignoreCaseLike "time" -> LocalTime::class.java.name
                javaType ignoreCaseLike "timezone" -> ZoneId::class.java.name
                javaType ignoreCaseLike "datetime" -> LocalDateTime::class.java.name
                else -> findRefType(javaType)
            }
            return s
        }

        private fun findRefType(javaType: String): String? {
            val javaClass = fieldMappings.find {
                val equalsIgnoreCase = it.javaClassSimple.equals(javaType,ignoreCase = true)
                equalsIgnoreCase
            }?.javaClassRef
            val containsAny = javaType.containsAny("Clob", "Object")
            val b = javaType ignoreCaseIn listOf("clob", "object")
            if (containsAny || b) {
                return String::class.java.name
            }
            if (javaClass == null) {
                println("未找到java类型${javaType} 的映射关系,请联系作者适配")
                return String::class.java.name
            }
            return javaClass
        }


        fun ktType2RefType(type: String): String {
            val find = fieldMappings.find {
                it.ktClassSimple.equals(type,ignoreCase = true)
            }
//            if (find == null) {
//                return String::class.java.name
//            }
            return find?.javaClassRef ?: String::class.java.name
        }


        var javaTypesEnum: Array<String>
            get() = fieldMappings.map { it.javaClassSimple }.distinct().toTypedArray()
            set(value) = TODO()


        var fieldMappings: List<FieldMapping> = listOf(
            FieldMapping(::isTextType, "text", "text", "clob", "CLOB", "text", "", String::class),
            FieldMapping(::isStringType, "varchar", "varchar", "varchar2", "VARCHAR", "varchar", "(255)", String::class),
            FieldMapping(::isCharType, "char", "character", "char", "VARCHAR", "character", "(255)", String::class),
            FieldMapping( ::isDateTimeType, "datetime", "timestamp with time zone", "timestamp", "TIMESTAMP", "timestamp", "", LocalDateTime::class ),
            FieldMapping(::isDateType, "date", "date", "date", "TIMESTAMP", "date", "", Date::class),
            FieldMapping(::isTimeType, "time", "time with time zone", "timestamp", "TIMESTAMP", "time", "", LocalTime::class),
            FieldMapping(::isIntType, "int", "integer", "number", "INT", "integer", "", Integer::class),
            FieldMapping( ::isDoubleType, "double", "double precision", "binary_double", "DOUBLE", "double precision", "(6,2)", Double::class ),
            FieldMapping(::isBigDecimalType, "decimal", "numeric", "number", "NUMERIC", "numeric", "(19,2)", BigDecimal::class),
            FieldMapping(::isLongType, "long", "bigint", "number", "BIGINT", "bigint", "", Long::class),
            FieldMapping(::isBooleanType, "boolean", "boolean", "number", "INT", "boolean", "", Boolean::class),
        ).onEach { mapping ->
            // 添加计算属性
            mapping.javaClassRef = mapping.classRef.java.name
            mapping.javaClassSimple = mapping.classRef.java.simpleName
        }
        var databaseType: HashMap<String, DatabaseDDLGenerator> = object : HashMap<String, DatabaseDDLGenerator>() {
            init {
                put(MYSQL, MysqlDDLGenerator())
                put(ORACLE, OracleDDLGenerator())
                put(POSTGRESQL, PostgreSQLDDLGenerator())
                put(DM, DMSQLDDLGenerator())
                put(H2, H2SQLDDLGenerator())
            }
        }

    }
}

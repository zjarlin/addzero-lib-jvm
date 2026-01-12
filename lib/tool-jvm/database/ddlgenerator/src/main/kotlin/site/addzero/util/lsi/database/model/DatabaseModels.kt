package site.addzero.util.lsi.database.model

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField

/**
 * 多对多关系中间表信息
 */
data class ManyToManyTable(
    /** 中间表名称 */
    val tableName: String,

    /** 左侧实体表名 */
    val leftTableName: String,

    /** 右侧实体表名 */
    val rightTableName: String,

    /** 中间表的左外键列名（指向左侧表） */
    val leftColumnName: String,

    /** 中间表的右外键列名（指向右侧表） */
    val rightColumnName: String,

    /** 左侧实体类 */
    val leftEntity: LsiClass? = null,

    /** 右侧实体类 */
    val rightEntity: LsiClass? = null,

    /** 字段 */
    val field: LsiField? = null,

    /** 左侧实体类名 */
    val leftClassName: String? = null,

    /** 右侧实体类名 */
    val rightClassName: String? = null
) {
    companion object {
        /**
         * 生成默认的中间表名
         * 格式: {leftTable}_{rightTable}（按字母顺序）
         */
        fun generateTableName(leftTable: String, rightTable: String): String {
            val tables = listOf(leftTable, rightTable).sorted()
            return "${tables[0]}_${tables[1]}"
        }
    }
}

/**
 * 外键信息
 */
data class ForeignKeyInfo(
    /** 外键名称 */
    val name: String,

    /** 外键列名 */
    val columnName: String,

    /** 引用的表名 */
    val referencedTableName: String,

    /** 引用的列名 */
    val referencedColumnName: String,

    /** ON DELETE 行为 */
    val onDelete: String? = null,

    /** ON UPDATE 行为 */
    val onUpdate: String? = null
)

/**
 * 索引类型
 */
enum class IndexType {
    /** 普通索引 */
    NORMAL,

    /** 唯一索引 */
    UNIQUE,

    /** 全文索引 */
    FULLTEXT
}

/**
 * 索引定义
 */
data class IndexDefinition(
    /** 索引名称 */
    val name: String,

    /** 索引列名列表 */
    val columns: List<String>,

    /** 索引类型 */
    val type: IndexType = IndexType.NORMAL,

)

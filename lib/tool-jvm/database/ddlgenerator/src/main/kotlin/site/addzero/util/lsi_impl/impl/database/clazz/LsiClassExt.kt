package site.addzero.util.lsi_impl.impl.database.clazz

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.database.model.ForeignKeyInfo
import site.addzero.util.lsi.database.model.IndexDefinition
import site.addzero.util.lsi.database.model.IndexType
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.field.getArg
import site.addzero.util.lsi.field.hasAnnotationIgnoreCase
import site.addzero.util.lsi_impl.impl.database.field.getForeignKeyInfo
import site.addzero.util.lsi_impl.impl.database.field.isDbField
import site.addzero.util.lsi_impl.impl.database.field.isPrimaryKey


/**
 * 获取主键列
 */
val LsiClass.primaryKeyColumn: LsiField?
    get() {
        val find = this.fields.find { it.isPrimaryKey }
        return find
    }

/**
 * 获取数据库字段列表
 * 过滤掉静态字段、集合类型字段
 * @return 数据库字段列表
 */
val LsiClass.dbFields get() = fields.filter { it.isDbField }

/**
 * 获取所有数据库字段（包括继承的字段）
 * 这个方法会递归获取父类的字段
 * @return 所有数据库字段列表
 */
fun LsiClass.getAllDbFields(): List<LsiField> {
    val result = mutableListOf<LsiField>()
    // 添加当前类的数据库字段
    result.addAll(dbFields)
    // 递归添加父类的数据库字段
    superClasses.forEach { superClass ->
        result.addAll(superClass.getAllDbFields())
    }
    return result
}


/**  获取主键名字 */
val LsiClass.primaryKeyName: String?
    get() {
        val map = this.fields.filter { it.isPrimaryKey }.map { it.name }.firstOrNull()
        return map
    }

/**
 * 获取数据库字段（排除静态、集合等非数据库字段）
 */
@Deprecated("Deprecated", replaceWith = ReplaceWith("getAllDbFields()"))
val LsiClass.databaseFields: List<LsiField>
    get() = fields.filter { it.isDbField }


/**
 * 获取所有非主键列
 */
val LsiClass.nonPrimaryColumns: List<LsiField>
    get() = fields.filter { !it.isPrimaryKey }


/**
 * 获取所有外键定义
 * 从字段的 @JoinColumn, @ManyToOne, @OneToOne 等注解中提取
 */
fun LsiClass.getDatabaseForeignKeys(): List<ForeignKeyInfo> {
    val allDbFields = getAllDbFields()
    val mapNotNull = allDbFields.mapNotNull {
        val foreignKeyInfo = it.getForeignKeyInfo()
        foreignKeyInfo
    }
    return mapNotNull
}


/**
 * 是否有Key注解（用于索引）
 */
val LsiField.isKey: Boolean
    get() = hasAnnotationIgnoreCase("Key")

/**
 * 获取Key注解的group参数
 * Jimmer支持 @Key(group = "groupName") 来创建联合索引
 */
val LsiField.keyGroup: String?
    get() {
        if (!isKey) return null
        return getArg("Key", "group")
    }

/**
 * 是否是唯一键
 */
val LsiField.isUnique: Boolean
    get() = hasAnnotationIgnoreCase("Unique") ||
            annotations.any {
                it.qualifiedName?.endsWith(".Column") == true &&
                        it.getAttribute("unique")?.toString()?.toBoolean() == true
            }

/**
 * 获取所有索引字段
 */
val LsiClass.indexFields: List<LsiField>
    get() = getAllDbFields().filter { it.isKey || it.isUnique }

/**
 * 生成索引定义
 *
 * 支持：
 * 1. 单字段 @Key - 生成单列唯一索引
 * 2. @Key(group="groupName") - 生成联合唯一索引
 * 3. @Unique - 生成唯一索引
 */
fun LsiClass.getIndexDefinitions(): List<IndexDefinition> {
    val indexes = mutableListOf<IndexDefinition>()
    val tableName = name?.lowercase() ?: "table"

    // ===== 1. 处理联合索引（@Key(group="xxx")） =====
    // 按group分组
    val keyFieldsByGroup = getAllDbFields()
        .filter { it.isKey && !it.isPrimaryKey && it.keyGroup != null }
        .groupBy { it.keyGroup!! }

    // 为每个group生成联合索引
    keyFieldsByGroup.forEach { (groupName, fields) ->
        val columns = fields.mapNotNull { it.columnName ?: it.name }
        if (columns.isNotEmpty()) {
            indexes.add(
                IndexDefinition(
                    name = "uk_${tableName}_${groupName}",
                    columns = columns,
                    type = IndexType.UNIQUE  // Jimmer的@Key是唯一键
                )
            )
        }
    }

    // ===== 2. 处理单字段索引（@Key不带group） =====
    getAllDbFields().forEach { field ->
        if (field.isPrimaryKey) return@forEach

        val columnName = field.columnName ?: field.name ?: return@forEach

        // 没有group的@Key注解生成单列唯一索引
        if (field.isKey && field.keyGroup == null) {
            indexes.add(
                IndexDefinition(
                    name = "uk_${tableName}_${columnName}",
                    columns = listOf(columnName),
                    type = IndexType.UNIQUE  // Jimmer的@Key是唯一键
                )
            )
        }

        // @Unique注解生成唯一索引
        if (field.isUnique && !field.isKey) {
            indexes.add(
                IndexDefinition(
                    name = "uk_${tableName}_${columnName}",
                    columns = listOf(columnName),
                    type = IndexType.UNIQUE
                )
            )
        }
    }

    return indexes
}

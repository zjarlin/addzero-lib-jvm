package site.addzero.util.lsi_impl.impl.database.field

import org.babyfish.jimmer.sql.JoinColumn
import site.addzero.util.lsi.assist.TypeChecker
import site.addzero.util.lsi.database.model.ForeignKeyInfo
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.field.getArg
import site.addzero.util.lsi.field.hasAnnotation
import site.addzero.util.lsi.field.hasAnnotationIgnoreCase
import site.addzero.util.str.addSuffixIfNot
import site.addzero.util.str.containsAnyIgnoreCase
import site.addzero.util.str.firstNotBlank


/**数据库无关列  */
val LsiField.isTransient: Boolean
    get() {
        this.hasAnnotation("Transient")
        return this.annotations.any {
            it.qualifiedName?.endsWith(".Transient") == true
        }
    }

/** 整数位数 */
val LsiField.precision: Int
    get() {
        val arg1 = this.getArg("Column", "precision")
        val arg = this.getArg("Precision")
        val firstNotBlank = firstNotBlank(arg1, arg)
        val toInt = firstNotBlank?.toInt()
        return toInt ?: -1
    }

/** 小数位数 */
val LsiField.scale: Int
    get() {
        val arg1 = this.getArg("Column", "scale")
        val arg = this.getArg("Scale")
        val firstNotBlank = firstNotBlank(arg1, arg)
        val toInt = firstNotBlank?.toInt()
        return toInt ?: -1
    }

/**  是否序列*/
val LsiField.iSsequence: Boolean
    get() {
        return isIdTypeByAnno("SEQUENCE")
    }

/**
 * 是否使用UUID生成器
 *
 * Jimmer方式：@GeneratedValue(generatorType = UUIDIdGenerator.class)
 */
val LsiField.isUUID: Boolean
    get() {
        if (!hasAnnotationIgnoreCase("GeneratedValue")) {
            return false
        }

        val generatorType = getArg("GeneratedValue", "generatorType")
        return generatorType?.contains("UUIDIdGenerator", ignoreCase = true) ?: false
    }

private fun LsiField.isIdTypeByAnno(strategy: String): Boolean {
    val arg = this.getArg("GeneratedValue", "strategy")
//        "GenerationType.IDENTITY"
    val contains = arg?.contains(strategy, ignoreCase = true)
    if (contains != null) {
        return contains
    }
    return false
}

/** 字段长度字符串 例如  (255) */
val LsiField.lengthStr: String
    get() {
        val length = this.length
        val toString = length.toString()
        return toString.addSuffixIfNot("(").addSuffixIfNot(")")
    }

/**
 * 判断是否为数据库字段
 * 数据库字段需要满足：非静态 && 非集合类型 && 非 Transient
 */
val LsiField.isDbField: Boolean
    get() = !isStatic && !isCollectionType && !isTransient


/**
 * 主键列
 * 更完善的实现：检查 @Id 注解或字段名为 "id"
 */
val LsiField.isPrimaryKey: Boolean
    get() {
        val hasAnnotation = hasAnnotationIgnoreCase("Id")
        val equals = name.equals("id", ignoreCase = true)
        return hasAnnotation || equals
    }

/**
 * 是否自增（数据库自动编号）
 *
 * Jimmer支持的方式：
 * 1. @GeneratedValue(strategy = GenerationType.IDENTITY)
 * 2. @GeneratedValue 不指定strategy（默认为IDENTITY）
 * 3. JPA的 @GeneratedValue(strategy = GenerationType.AUTO)
 */
val LsiField.isAutoIncrement: Boolean
    get() {
        if (!hasAnnotationIgnoreCase("GeneratedValue")) {
            return false
        }

        // 如果指定了generatorType，说明使用的是自定义生成器或UUID生成器，不是自增
        val generatorType = getArg("GeneratedValue", "generatorType")
        if (generatorType != null) {
            return false
        }

        val strategy = getArg("GeneratedValue", "strategy")

        // 如果没有指定strategy，默认为IDENTITY（Jimmer默认）
        if (strategy == null) {
            return true
        }

        // 检查是否为IDENTITY或AUTO
        return strategy.containsAnyIgnoreCase("IDENTITY", "AUTO")
    }

/**
 * 是否使用序列生成ID
 *
 * Jimmer方式：@GeneratedValue(strategy = GenerationType.SEQUENCE, generatorName = "seq_name")
 */
val LsiField.isSequence: Boolean
    get() {
        if (!hasAnnotationIgnoreCase("GeneratedValue")) {
            return false
        }

        val strategy = getArg("GeneratedValue", "strategy")
        return strategy?.contains("SEQUENCE", ignoreCase = true) ?: false
    }

/**
 * 获取序列名称
 *
 * 从 @GeneratedValue(generatorName = "xxx") 获取
 */
val LsiField.sequenceName: String?
    get() {
        if (!isSequence) {
            return null
        }
        return getArg("GeneratedValue", "generatorName")
    }

/**
 * 是否使用UUID生成器
 *
 * Jimmer方式：@GeneratedValue(generatorType = UUIDIdGenerator.class)
 */

/**
 * 是否使用用户自定义的IdGenerator
 *
 * Jimmer方式：@GeneratedValue(generatorType = CustomIdGenerator.class)
 */
val LsiField.hasCustomIdGenerator: Boolean
    get() {
        if (!hasAnnotationIgnoreCase("GeneratedValue")) {
            return false
        }

        val generatorType = getArg("GeneratedValue", "generatorType")
        // 如果有generatorType但不是UUIDIdGenerator，说明是自定义的
        return generatorType != null && !generatorType.contains("UUIDIdGenerator", ignoreCase = true)
    }

/**
 * 获取自定义ID生成器的类型
 */
val LsiField.customIdGeneratorType: String?
    get() {
        if (!hasCustomIdGenerator) {
            return null
        }
        return getArg("GeneratedValue", "generatorType")
    }
/**
 * 获取外键信息
 * 从 @ManyToOne, @OneToOne, @JoinColumn 等注解中提取外键信息
 */
fun LsiField.getForeignKeyInfo(): ForeignKeyInfo? {
    val joinColumnAnno = annotations.firstOrNull {
        it.qualifiedName in setOf(
            JoinColumn::class.qualifiedName!!
        )
    } ?: return null

    val columnName = this.columnName ?: this.name ?: return null
    val referencedTable = joinColumnAnno.getAttribute("name")?.toString() ?: return null
    val referencedColumn = joinColumnAnno.getAttribute("referencedColumnName")?.toString() ?: "id"

    val fkName = "fk_${columnName}_${referencedTable}"

    val foreignKeyInfo = ForeignKeyInfo(
        name = fkName,
        columnName = columnName,
        referencedTableName = referencedTable,
        referencedColumnName = referencedColumn
    )
    return foreignKeyInfo
}

/**
 * 字段长度
 * 从 @Length 或 @Column(length=xxx) 注解中获取
 */
val LsiField.length: Int
    get() {
        // 优先从 @Length 注解获取
        getArg("Length", "value")?.toIntOrNull()?.let { return it }
        getArg("Length", "max")?.toIntOrNull()?.let { return it }

        // 从 @Column(length=xxx) 获取
        getArg("Column", "length")?.toIntOrNull()?.let { return it }

        return -1
    }

/**
 * 是否为长文本字段
 * 满足以下条件之一即为长文本：
 * 1. 字段类型为 String 且 @Length 或 @Column(length) 的值 > 1000
 * 2. 有 @Lob 注解
 * 3. @Column(columnDefinition) 包含 TEXT/CLOB 关键字
 */
val LsiField.isText: Boolean
    get() {
        val typeName = this.typeName ?: return false

        // 必须是字符串类型
        if (!TypeChecker.isStringType(typeName)) {
            return false
        }

        // 检查 @Lob 注解
        if (hasAnnotationIgnoreCase("Lob")) {
            return true
        }

        // 检查 @Column(columnDefinition) 是否包含 TEXT/CLOB
        val columnDef = getArg("Column", "columnDefinition")
        if (columnDef != null && columnDef.containsAnyIgnoreCase("TEXT", "CLOB", "LONGTEXT", "MEDIUMTEXT")) {
            return true
        }

        // 检查长度是否超过阈值 (1000)
        val fieldLength = length
        if (fieldLength > 1000) {
            return true
        }

        return false
    }

/**
 * 猜测字段长度
 */
val LsiField.guessLength: Int
    get() {
        val explicitLength = length
        if (explicitLength > 0) {
            return explicitLength
        }

        val typeName = this.typeName ?: return 255
        return when {
            TypeChecker.isStringType(typeName) -> 255
            TypeChecker.isIntType(typeName) -> 11
            TypeChecker.isLongType(typeName) -> 20
            TypeChecker.isFloatType(typeName) || TypeChecker.isDoubleType(typeName) -> 10
            TypeChecker.isBooleanType(typeName) -> 0
            TypeChecker.isDateTimeType(typeName) -> 0
            else -> 255
        }
    }


val LsiField.isString: Boolean
    get() {
        val typeName = this.typeName ?: return false
        return typeName == "java.lang.String" || typeName.substringAfterLast('.').equals("string", ignoreCase = true)
    }

/**
 * 判断是否为长文本类型
 */
fun LsiField.isTextType(): Boolean {
    val textKeywords = listOf("url", "base64", "text", "path", "introduction", "content", "description")
    return textKeywords.any { name?.contains(it, ignoreCase = true) ?: false }
}


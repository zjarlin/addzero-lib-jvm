package site.addzero.lsi.assist

import site.addzero.lsi.types.TypeRegistry
import site.addzero.lsi.types.PrimitiveType

/**
 * 类型检查器 - 统一的类型判断工具
 *
 * 提供各种类型的判断方法，支持：
 * - 基本类型（primitive types）
 * - 包装类型（wrapper types）
 * - 集合类型（collection types）
 * - 日期时间类型（date/time types）
 * - 数值类型（numeric types）
 * - 自定义对象类型（custom object types）
 */
object TypeChecker {

    // ============ 基本类型判断 ============

    /**
     * 判断是否为Kotlin基本类型
     */
    fun isKotlinPrimitiveType(simpleTypeName: String): Boolean {
        return PrimitiveType.findByName(simpleTypeName)?.kotlinName?.equals(simpleTypeName, ignoreCase = true) == true
    }

    /**
     * 判断是否为Java基本类型（含包装类）
     */
    fun isJavaPrimitiveType(typeName: String): Boolean {
        return TypeRegistry.isPrimitive(typeName)
    }

    // ============ 数值类型判断 ============

    /**
     * 判断是否为整型
     */
    fun isIntType(javaType: String): Boolean {
        return TypeRegistry.isInteger(javaType)
    }

    /**
     * 判断是否为长整型
     */
    fun isLongType(javaType: String): Boolean {
        val simpleType = javaType.substringAfterLast('.').lowercase()
        return simpleType == "long"
    }

    /**
     * 判断是否为浮点类型
     */
    fun isFloatType(javaType: String?): Boolean {
        javaType?: return false
        return TypeRegistry.isFloatingPoint(javaType)
    }

    /**
     * 判断是否为双精度浮点类型
     */
    fun isDoubleType(javaType: String?): Boolean {
        javaType?: return false
        val toSimpleName = javaType.toSimpleName()
        val simpleType = toSimpleName.lowercase()
        return simpleType in setOf("double", "float")
    }

    /**
     * 判断是否为数值类型（包括整型、浮点型、BigDecimal等）
     */
    fun isNumericType(typeName: String?): Boolean {
        typeName?: return false
        return TypeRegistry.isNumeric(typeName)
    }

    /**
     * 判断是否为BigDecimal类型
     */
    fun isBigDecimalType(javaType: String?): Boolean {
        javaType?: return false
        return TypeRegistry.isBigNumber(javaType) && javaType.contains("BigDecimal", ignoreCase = true)
    }

    // ============ 字符类型判断 ============
    /**
     * 判断是否为字符类型
     */
    fun isCharType(javaType: String?): Boolean {
        javaType?: return false
        return TypeRegistry.isChar(javaType)
    }

    /**
     * 判断是否为字符串类型
     */
    fun isStringType(typeName: String?): Boolean {
        typeName?: return false
        return TypeRegistry.isString(typeName)
    }

    fun String.toSimpleName(): String {
        val substringAfterLast = this.substringAfterLast('.')
        return substringAfterLast
    }


    /**
     * 判断是否为长文本类型（根据字段名推断）
     */
    fun isTextType(javaType: String, fieldName: String): Boolean {
        if (!isStringType(javaType)) return false
        return TEXT_KEYWORDS.any { fieldName.contains(it, ignoreCase = true) }
    }

    // ============ 布尔类型判断 ============

    /**
     * 判断是否为布尔类型
     */
    fun isBooleanType(typeName: String): Boolean {
        return TypeRegistry.isBoolean(typeName)
    }

    // ============ 日期时间类型判断 ============

    /**
     * 判断是否为日期类型
     */
    fun isDateType(javaType: String): Boolean {
        return TypeRegistry.isDate(javaType)
    }

    /**
     * 判断是否为时间类型
     */
    fun isTimeType(javaType: String): Boolean {
        return TypeRegistry.isTime(javaType)
    }

    /**
     * 判断是否为日期时间类型
     */
    fun isDateTimeType(typeName: String): Boolean {
        return TypeRegistry.isDateTime(typeName)
    }

    // ============ 集合类型判断 ============

    /**
     * 判断是否为集合类型
     */
    fun isCollectionType(typeName: String?): Boolean {
        typeName ?: return false
        return TypeRegistry.isCollection(typeName)
    }

    /**
     * 判断是否为数组类型
     */
    fun isArrayType(typeName: String): Boolean {
        return TypeRegistry.isArray(typeName)
    }

    // ============ 自定义对象类型判断 ============

    /**
     * 判断是否为自定义对象类型（非基本类型、非集合、非常用类型）
     */
    fun isCustomObjectType(typeName: String): Boolean {
        val simpleType = typeName.substringAfterLast('.')
        return simpleType !in COMMON_TYPES &&
                !isCollectionType(typeName) &&
                !isArrayType(typeName)
    }

    // ============ 常量定义 ============

    private val TEXT_KEYWORDS = listOf(
        "url", "base64", "text", "path", "introduction", "content", "description"
    )

    private val COMMON_TYPES = setOf(
        "Int", "Boolean", "Double", "Float", "Long", "String", "Any",
        "Byte", "Short", "Char"
    )
}

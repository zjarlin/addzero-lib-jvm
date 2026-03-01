package site.addzero.lsi.types

/**
 * 统一的类型查询注册中心
 *
 * 提供类型安全的类型判断和查询API，替代散落在各处的字符串比较
 */
object TypeRegistry {
    // ============ 基本类型查询 ============

    fun findPrimitiveType(name: String): PrimitiveType? = PrimitiveType.findByName(name)
    fun isPrimitive(name: String): Boolean = PrimitiveType.isPrimitive(name)

    // ============ 数值类型查询 ============

    fun findNumericType(name: String): NumericType? = NumericType.findByName(name)
    fun isNumeric(name: String): Boolean = NumericType.isNumeric(name)
    fun isInteger(name: String): Boolean = findNumericType(name)?.isInteger == true
    fun isFloatingPoint(name: String): Boolean = findNumericType(name)?.isFloatingPoint == true
    fun isBigNumber(name: String): Boolean = findNumericType(name)?.isBigNumber == true

    // ============ 日期时间类型查询 ============

    fun findDateTimeType(name: String): DateTimeType? = DateTimeType.findByName(name)
    fun isDateTime(name: String): Boolean = DateTimeType.isDateTime(name)
    fun isDate(name: String): Boolean = findDateTimeType(name)?.isDate == true
    fun isTime(name: String): Boolean = findDateTimeType(name)?.isTime == true

    // ============ 集合类型查询 ============

    fun isCollection(name: String): Boolean = CollectionType.isCollection(name)
    fun isArray(name: String): Boolean = CollectionType.isArray(name)

    // ============ 字符串类型查询 ============

    fun isString(name: String): Boolean =
        name == "java.lang.String" || name.substringAfterLast('.') == "String"

    // ============ 布尔类型查询 ============

    fun isBoolean(name: String): Boolean {
        val simpleName = name.substringAfterLast('.').lowercase()
        return simpleName == "boolean"
    }

    // ============ 字符类型查询 ============

    fun isChar(name: String): Boolean {
        val simpleName = name.substringAfterLast('.').lowercase()
        return simpleName in setOf("char", "character")
    }

    // ============ 默认值获取 ============

    /**
     * 根据类型名获取默认值
     */
    fun getDefaultValue(typeName: String): Any {
        // 优先尝试基本类型
        findPrimitiveType(typeName)?.let { return it.defaultValue }

        // 尝试数值类型
        findNumericType(typeName)?.let { return it.defaultValue }

        // 尝试日期时间类型
        findDateTimeType(typeName)?.let { return it.getDefaultValue() }

        // 字符串类型
        if (isString(typeName)) return ""

        // 未知类型返回类型名本身
        return typeName
    }
}

package site.addzero.lsi.assist

import site.addzero.lsi.types.TypeRegistry

/**
 * 根据类型名称返回默认值的字符串表示
 * 这是一个便捷函数，内部调用 getDefaultAnyValueForType 并转换为字符串
 *
 * @param typeName 类型名称，可以是简单名称（如 "String", "Int"）或全限定名（如 "java.lang.String"）
 * @return 默认值的字符串表示
 */
fun getDefaultValueForType(typeName: String): String {
    return getDefaultAnyValueForType(typeName).toString()
}

/**
 * 根据类型名称返回对应的默认值对象
 * 支持：
 * - Java 基本类型和包装类型：int, Integer, long, Long, double, Double, float, Float, boolean, Boolean, byte, Byte, short, Short, char, Character
 * - Kotlin 基本类型：Int, Boolean, Byte, Char, Double, Float, Long, Short
 * - 常用类型：String, Date, LocalDate, LocalDateTime, BigDecimal
 * - 全限定名：java.lang.Integer, java.util.Date 等
 *
 * @param typeName 类型名称（不区分大小写，支持简单名称和全限定名）
 * @return 类型对应的默认值对象
 */
fun getDefaultAnyValueForType(typeName: String): Any {
    return TypeRegistry.getDefaultValue(typeName)
}

/**
 * 根据类型名称判断是否为基本数值类型
 *
 * @param typeName 类型名称
 * @return 如果是数值类型返回 true，否则返回 false
 * @deprecated Use TypeChecker.isNumericType instead
 */
@Deprecated(
    message = "Use TypeChecker.isNumericType instead",
    replaceWith = ReplaceWith("TypeChecker.isNumericType(typeName)", "site.addzero.lsi.assist.TypeChecker")
)
fun isNumericType(typeName: String): Boolean {
    return TypeChecker.isNumericType(typeName)
}

/**
 * 根据类型名称判断是否为布尔类型
 *
 * @param typeName 类型名称
 * @return 如果是布尔类型返回 true，否则返回 false
 * @deprecated Use TypeChecker.isBooleanType instead
 */
@Deprecated(
    message = "Use TypeChecker.isBooleanType instead",
    replaceWith = ReplaceWith("TypeChecker.isBooleanType(typeName)", "site.addzero.lsi.assist.TypeChecker")
)
fun isBooleanType(typeName: String): Boolean {
    return TypeChecker.isBooleanType(typeName)
}

/**
 * 根据类型名称判断是否为字符串类型
 *
 * @param typeName 类型名称
 * @return 如果是字符串类型返回 true，否则返回 false
 * @deprecated Use TypeChecker.isStringType instead
 */
@Deprecated(
    message = "Use TypeChecker.isStringType instead",
    replaceWith = ReplaceWith("TypeChecker.isStringType(typeName)", "site.addzero.lsi.assist.TypeChecker")
)
fun isStringType(typeName: String): Boolean {
    return TypeChecker.isStringType(typeName)
}

/**
 * 根据类型名称判断是否为日期时间类型
 *
 * @param typeName 类型名称
 * @return 如果是日期时间类型返回 true，否则返回 false
 * @deprecated Use TypeChecker.isDateTimeType instead
 */
@Deprecated(
    message = "Use TypeChecker.isDateTimeType instead",
    replaceWith = ReplaceWith("TypeChecker.isDateTimeType(typeName)", "site.addzero.lsi.assist.TypeChecker")
)
fun isDateTimeType(typeName: String): Boolean {
    return TypeChecker.isDateTimeType(typeName)
}

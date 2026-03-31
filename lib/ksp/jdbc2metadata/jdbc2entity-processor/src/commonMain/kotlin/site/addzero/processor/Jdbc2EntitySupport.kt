package site.addzero.processor

import site.addzero.context.Settings
import site.addzero.util.str.containsAny
import site.addzero.util.str.toLowCamelCase
import site.addzero.util.str.withPkg

/**
 * JDBC 实体生成器的本地配置辅助。
 */
internal val Settings.modelOutputDir: String
    get() = backendModelSourceDir.withPkg(modelPackageName)

/**
 * 判断列是否应该由生成器保留。
 */
internal fun shouldKeepGeneratedColumn(columnName: String): Boolean {
    if (columnName.isEmpty()) {
        return false
    }

    val configuredColumns = listOf(
        Settings.id,
        Settings.createBy,
        Settings.updateBy,
        Settings.createTime,
        Settings.updateTime,
    ).filter { it.isNotBlank() }

    if (configuredColumns.isEmpty()) {
        return true
    }

    val lowerCamelColumns = configuredColumns.map { it.toLowCamelCase() }
    val matchesConfiguredName = columnName.containsAny(*configuredColumns.toTypedArray())
    val matchesLowerCamelName = columnName.containsAny(*lowerCamelColumns.toTypedArray())
    return !(matchesConfiguredName || matchesLowerCamelName)
}

/**
 * JDBC 列类型到 Kotlin 类型的映射。
 */
internal fun mapJdbcColumnType(columnType: String, isKmp: Boolean = true): String {
    val localDateStr = if (isKmp) "kotlinx.datetime.LocalDate" else "java.time.LocalDate"
    val localTimeStr = if (isKmp) "kotlinx.datetime.LocalTime" else "java.time.LocalTime"
    val localDateTimeStr = if (isKmp) "kotlinx.datetime.LocalDateTime" else "java.time.LocalDateTime"

    return when {
        columnType.contains("char", ignoreCase = true) -> "String"
        columnType.contains("varchar", ignoreCase = true) -> "String"
        columnType.contains("text", ignoreCase = true) -> "String"
        columnType.contains("bigint", ignoreCase = true) -> "Long"
        columnType.contains("int", ignoreCase = true) -> "Long"
        columnType.contains("int8", ignoreCase = true) -> "Long"
        columnType.contains("smallint", ignoreCase = true) -> "Short"
        columnType.contains("float", ignoreCase = true) -> "Float"
        columnType.contains("double", ignoreCase = true) -> "Double"
        columnType.contains("real", ignoreCase = true) -> "Float"
        columnType.contains("bool", ignoreCase = true) -> "Boolean"
        columnType.contains("timestamp", ignoreCase = true) -> localDateTimeStr
        columnType.contains("date", ignoreCase = true) -> localDateStr
        columnType.contains("time", ignoreCase = true) -> localTimeStr
        else -> "String"
    }
}

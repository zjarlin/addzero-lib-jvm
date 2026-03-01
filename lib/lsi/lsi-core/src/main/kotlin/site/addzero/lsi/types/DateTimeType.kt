package site.addzero.lsi.types

import java.util.*

/**
 * 日期时间类型的强类型枚举
 *
 * 包括java.util.Date、java.time.*等日期时间类型
 */
enum class DateTimeType(
    val fqName: String,
    val category: DateTimeCategory
) {
    JAVA_UTIL_DATE("java.util.Date", DateTimeCategory.DATETIME),
    JAVA_SQL_DATE("java.sql.Date", DateTimeCategory.DATE),
    JAVA_SQL_TIME("java.sql.Time", DateTimeCategory.TIME),
    JAVA_SQL_TIMESTAMP("java.sql.Timestamp", DateTimeCategory.DATETIME),
    LOCAL_DATE("java.time.LocalDate", DateTimeCategory.DATE),
    LOCAL_TIME("java.time.LocalTime", DateTimeCategory.TIME),
    LOCAL_DATETIME("java.time.LocalDateTime", DateTimeCategory.DATETIME),
    ZONED_DATETIME("java.time.ZonedDateTime", DateTimeCategory.DATETIME),
    OFFSET_DATETIME("java.time.OffsetDateTime", DateTimeCategory.DATETIME);

    val isDate: Boolean get() = category == DateTimeCategory.DATE
    val isTime: Boolean get() = category == DateTimeCategory.TIME
    val isDateTime: Boolean get() = category == DateTimeCategory.DATETIME

    /**
     * 获取该类型的默认值
     */
    fun getDefaultValue(): Any = when (this) {
        JAVA_UTIL_DATE, JAVA_SQL_DATE, JAVA_SQL_TIMESTAMP -> Date().time
        LOCAL_DATE, JAVA_SQL_DATE -> "2024-03-22"
        LOCAL_DATETIME, ZONED_DATETIME, OFFSET_DATETIME -> "2024-03-22 12:00:00"
        LOCAL_TIME, JAVA_SQL_TIME -> "12:00:00"
    }

    companion object {
        private val byFqName = entries.associateBy { it.fqName }
        private val bySimpleName = entries.associateBy { it.fqName.substringAfterLast('.').lowercase() }
        private val byCategory = entries.groupBy { it.category }

        fun findByName(name: String): DateTimeType? {
            return byFqName[name] ?: bySimpleName[name.substringAfterLast('.').lowercase()]
        }

        fun isDateTime(name: String): Boolean = findByName(name) != null

        fun allInCategory(category: DateTimeCategory): List<DateTimeType> = byCategory[category].orEmpty()

        val allDateTypes: List<DateTimeType> get() = allInCategory(DateTimeCategory.DATE)
        val allTimeTypes: List<DateTimeType> get() = allInCategory(DateTimeCategory.TIME)
        val allDateTimeTypes: List<DateTimeType> get() = allInCategory(DateTimeCategory.DATETIME)

        val allFqNames: Set<String> get() = byFqName.keys
    }
}

/**
 * 日期时间类型的分类
 */
enum class DateTimeCategory {
    /** 纯日期 */
    DATE,
    /** 纯时间 */
    TIME,
    /** 日期时间 */
    DATETIME
}

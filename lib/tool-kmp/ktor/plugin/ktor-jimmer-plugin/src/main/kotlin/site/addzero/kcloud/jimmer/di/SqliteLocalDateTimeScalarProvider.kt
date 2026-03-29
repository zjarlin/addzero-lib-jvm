package site.addzero.kcloud.jimmer.di

import org.babyfish.jimmer.sql.runtime.AbstractScalarProvider
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

internal val SQLITE_LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
    .optionalEnd()
    .toFormatter()

object SqliteLocalDateTimeScalarProvider :
    AbstractScalarProvider<LocalDateTime, String>(LocalDateTime::class.java, String::class.java) {

    override fun toScalar(sqlValue: String): LocalDateTime {
        val normalized = sqlValue.trim()
        if (normalized.isEmpty()) {
            throw IllegalArgumentException("SQLite datetime column cannot be blank")
        }
        if (normalized.all(Char::isDigit)) {
            return Instant.ofEpochMilli(normalized.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }
        return runCatching {
            LocalDateTime.parse(normalized, SQLITE_LOCAL_DATE_TIME_FORMATTER)
        }.getOrElse {
            LocalDateTime.parse(normalized)
        }
    }

    override fun toSql(scalarValue: LocalDateTime): String {
        return SQLITE_LOCAL_DATE_TIME_FORMATTER.format(scalarValue)
    }
}

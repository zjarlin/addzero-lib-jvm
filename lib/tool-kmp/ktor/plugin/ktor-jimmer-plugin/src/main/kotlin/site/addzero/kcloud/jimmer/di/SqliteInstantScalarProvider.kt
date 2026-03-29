package site.addzero.kcloud.jimmer.di

import org.babyfish.jimmer.sql.runtime.AbstractScalarProvider
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object SqliteInstantScalarProvider :
    AbstractScalarProvider<Instant, String>(Instant::class.java, String::class.java) {

    override fun toScalar(sqlValue: String): Instant {
        val normalized = sqlValue.trim()
        if (normalized.isEmpty()) {
            throw IllegalArgumentException("SQLite datetime column cannot be blank")
        }
        if (normalized.all(Char::isDigit)) {
            return Instant.ofEpochMilli(normalized.toLong())
        }
        val parsed = runCatching {
            LocalDateTime.parse(normalized, SQLITE_LOCAL_DATE_TIME_FORMATTER)
        }.getOrElse {
            LocalDateTime.parse(normalized)
        }
        return parsed.atZone(ZoneId.systemDefault()).toInstant()
    }

    override fun toSql(scalarValue: Instant): String {
        return SQLITE_LOCAL_DATE_TIME_FORMATTER.format(
            LocalDateTime.ofInstant(scalarValue, ZoneId.systemDefault()),
        )
    }
}

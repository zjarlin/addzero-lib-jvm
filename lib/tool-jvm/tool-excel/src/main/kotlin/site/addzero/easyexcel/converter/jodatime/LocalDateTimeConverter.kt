package site.addzero.easyexcel.converter.jodatime

import site.addzero.easyexcel.converter.jodatime.AbstractDateTimeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeConverter : AbstractDateTimeConverter<LocalDateTime?>() {

    override val formatter: DateTimeFormatter?
        get() = FORMATTER

    override fun parse(value: String?): LocalDateTime? {
        value?:return null
        return LocalDateTime.parse(value, FORMATTER)
    }

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}

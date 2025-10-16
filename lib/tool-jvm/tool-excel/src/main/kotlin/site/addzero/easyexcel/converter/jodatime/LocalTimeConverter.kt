package site.addzero.easyexcel.converter.jodatime

import site.addzero.easyexcel.converter.jodatime.AbstractDateTimeConverter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeConverter : AbstractDateTimeConverter<LocalTime?>() {

    override val formatter: DateTimeFormatter?
        get() = FORMATTER

    override fun parse(value: String?): LocalTime? {
        return LocalTime.parse(value, FORMATTER)
    }

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}

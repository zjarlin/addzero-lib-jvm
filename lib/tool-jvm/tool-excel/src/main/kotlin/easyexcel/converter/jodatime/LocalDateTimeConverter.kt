package com.gisroad.business.util.easyexcel.converter.jodatime

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

package com.gisroad.business.util.easyexcel.converter.jodatime

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

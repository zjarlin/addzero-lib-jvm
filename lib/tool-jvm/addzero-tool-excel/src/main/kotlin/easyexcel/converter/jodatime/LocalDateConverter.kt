package com.gisroad.business.util.easyexcel.converter.jodatime

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateConverter : AbstractDateTimeConverter<LocalDate?>() {
    override val formatter: DateTimeFormatter?
        get() = FORMATTER

    override fun parse(value: String?): LocalDate? {
        if (value != null) {
          return  LocalDate.parse(value, FORMATTER)
        }
        return null
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

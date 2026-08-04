package site.addzero.network.call.weatherutil

import cn.idev.excel.EasyExcel
import cn.idev.excel.ExcelWriter
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZhengzhouAnnualWeatherWorkbookTest {
    @Test
    fun `导出郑州2017到2018年度天气工作簿`() {
        val workbookPath = desktopOutputPath()
        val weatherByYear = listOf(2017, 2018).associateWith { year ->
            queryAnnualWeather(year)
        }

        val writer = EasyExcel.write(workbookPath.toFile()).build()
        try {
            weatherByYear.entries.forEachIndexed { index, entry ->
                writeYearSheet(writer, index, entry.key, entry.value)
            }
        } finally {
            writer.finish()
        }

        assertTrue(Files.exists(workbookPath), "天气工作簿应输出到桌面: ${workbookPath.absolutePathString()}")
        assertEquals(365, weatherByYear.getValue(2017).size)
        assertEquals(365, weatherByYear.getValue(2018).size)
        println("郑州2017-2018年度天气工作簿: ${workbookPath.absolutePathString()}")
    }

    private fun queryAnnualWeather(year: Int): List<WeatherData> {
        return (1..12).flatMap { month ->
            val monthData = WeatherUtil.queryWeather(
                year = year.toString(),
                month = month.toString(),
                areaId = ZHENGZHOU_AREA_ID,
                areaType = DOMESTIC_AREA_TYPE
            )

            monthData.filterNotNull()
        }
    }

    private fun writeYearSheet(
        writer: ExcelWriter,
        sheetIndex: Int,
        year: Int,
        weatherItems: List<WeatherData>
    ) {
        val sheetName = "郑州${year}"
        val rows = weatherItems.map { weather ->
            listOf(
                weather.date,
                weather.week.orEmpty(),
                weather.highTemp,
                weather.lowTemp,
                weather.amCondition,
                weather.pmCondition,
                weather.wind,
                weather.aqi,
                weather.areaId.orEmpty(),
                weather.areaType.orEmpty()
            )
        }
        val sheet = EasyExcel.writerSheet(sheetIndex, sheetName)
            .head(WEATHER_HEADERS.map { listOf(it) })
            .build()

        writer.write(rows, sheet)
    }

    private fun desktopOutputPath(): Path {
        val desktop = Paths.get(System.getProperty("user.home"), "Desktop")
        desktop.createDirectories()
        return desktop.resolve("郑州2017-2018年度天气.xlsx")
    }

    private companion object {
        private const val ZHENGZHOU_AREA_ID = "57083"
        private const val DOMESTIC_AREA_TYPE = "2"
        private val WEATHER_HEADERS = listOf(
            "日期",
            "星期",
            "最高温度",
            "最低温度",
            "上午天气",
            "下午天气",
            "风力风向",
            "空气质量指数",
            "地区ID",
            "地区类型"
        )
    }
}

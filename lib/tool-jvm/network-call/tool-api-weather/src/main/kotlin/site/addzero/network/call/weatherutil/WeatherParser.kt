package site.addzero.network.call.weatherutil

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.stream.Collectors

object WeatherParser {
    fun parseHtml(html: String?): MutableList<WeatherData?> {
        val doc = Jsoup.parse(html!!)
        val table = doc.select("table.history-table")
        // 解析历史天气数据表格
        val rows = table.select("tr")
        // 去掉表头行
        if (rows.isNotEmpty()) {
            rows.removeAt(0)
        }
        // 解析每一行数据，构造 WeatherData 对象
        return rows.stream().map<WeatherData?> { row: Element? ->
            val cells = row!!.select("td")
            // 确保有足够的单元格数据
            if (cells.size < 6) {
                // 如果单元格不足，跳过这一行
                return@map null
            }
            
            val date = cells.get(0).text()
            val highTemp = cells.get(1).text().replace("°", "").toIntOrNull() ?: 0
            val lowTemp = cells.get(2).text().replace("°", "").toIntOrNull() ?: 0
            val amCondition = cells.get(3).text()
            val pmCondition = cells.get(3).text()
            val wind = cells.get(4).text()
            val span = cells.get(5).select("span").text()
            val aqi = if (span == "-" || span.isEmpty()) {
                0
            } else {
                span.split(" ".toRegex()).firstOrNull()?.toIntOrNull() ?: 0
            }
            
            val weatherData = WeatherData(
                date = date,
                highTemp = highTemp,
                lowTemp = lowTemp,
                amCondition = amCondition,
                pmCondition = pmCondition,
                wind = wind,
                aqi = aqi
            )
            weatherData
        }.filter { it != null }.collect(Collectors.toList())
    }
}
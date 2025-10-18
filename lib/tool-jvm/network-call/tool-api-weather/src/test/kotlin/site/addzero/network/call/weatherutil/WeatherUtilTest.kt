package site.addzero.network.call.weatherutil

import cn.hutool.http.HttpUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import site.addzero.network.call.citys.AreaType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherUtilTest {


    @Test
    fun `test query weather with keyword and int area type1`() {
        // 使用关键字和Int类型的areaType查询天气
        val results = WeatherUtil.queryWeather("2025", "9", "洛阳")
        results.forEach { result ->
            println(result)
        }
    }



    @Test
    fun `test query weather with keyword and int area type`() {
        // 使用关键字和Int类型的areaType查询天气
        val results = WeatherUtil.queryWeather("2025", "9", "北京")
        println("Found ${results.size} city results for keyword '北京'")
        results.forEach { result ->
            println(result)
        }
    }

    @Test
    fun `test hhh`() {
        val urlString = "https://tianqi.2345.com/tqpcimg/tianqiimg/theme4/js/citySelectData2.js"
        val get = HttpUtil.get(urlString)
        println(get)
    }
}

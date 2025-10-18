package site.addzero.network.call.weatherutil

import cn.hutool.http.HttpUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherUtilTest {
    @Test
    fun `test djaosdjo` (){

    }

    @Test
    fun `test query weather with valid parameters`() {
//            val result = WeatherUtil.queryWeather("2025", "9", "71778", "2")
        val result = WeatherUtil.queryWeather("2025", "9", "7894", "1")

            println(result)
    }

    @Test
    fun `test hhh` (){
        val urlString = "https://tianqi.2345.com/tqpcimg/tianqiimg/theme4/js/citySelectData2.js"
        val get = HttpUtil.get(urlString)
        println(get)
    }

}

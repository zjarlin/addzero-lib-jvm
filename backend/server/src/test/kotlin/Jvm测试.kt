import com.addzero.common.util.excel.ExcelUtil
import com.addzero.web.infra.jimmer.toMap
import org.junit.jupiter.api.Test
import weatherutil.WeatherUtil

class Jvm测试 {

    @Test
    fun test1() {
//        val message = System.getenv("SERVER_HOST")
        val message = System.getenv("user.dir")
        val getenv = System.getenv("user.home")
        println(message)

    }


    @Test
    fun test2() {

        val flatMap = (7..12).flatMap {
            val queryWeather = WeatherUtil.queryWeather("2023", it.toString(),
                "57073", "2")
            queryWeather
        }
        val flatMap1 = (1..4).flatMap {
            val queryWeather = WeatherUtil.queryWeather(
                "2024", it.toString(),
                "57073", "2"
            )
            queryWeather

        }
        val data = flatMap + flatMap1


        val toMap = data.map { it.toMap() }
        ExcelUtil.exportMapList(toMap, "/Users/zjarlin/Desktop/outtt/112.xlsx")

    }

}

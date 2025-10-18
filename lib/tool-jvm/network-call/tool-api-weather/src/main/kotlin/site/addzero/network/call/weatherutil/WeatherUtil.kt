package site.addzero.network.call.weatherutil

import cn.hutool.http.HttpUtil
import com.alibaba.fastjson2.JSON
import site.addzero.network.call.citys.AreaType
import site.addzero.network.call.citys.CityService
import site.addzero.network.call.weatherutil.Assist.isFutureDate
import site.addzero.network.call.weatherutil.Assist.splitDateAndWeekday

/**
 * 2345天气查询接口
 *
 * @author zjarlin
 * @since 2023/04/09
 */
object WeatherUtil {
    // 使用lazy延迟初始化，避免内存泄漏
    private val cityService by lazy { CityService() }

    /**
     * 根据关键字查询天气
     *
     * @param year     年份
     * @param month    月份
     * @param keyword  查询关键字（城市名、区域名等）
     * @param areaType 地区类型枚举 (DOMESTIC 或 INTERNATIONAL)
     * @return 天气数据列表
     */
    fun queryWeather(year: String, month: String, keyword: String, areaType: AreaType = AreaType.DOMESTIC): List<MutableList<WeatherData?>> {
        val areas = cityService.searchCities(keyword, areaType)
        return areas.map { area ->
            queryWeatherByAreaId(year, month, area.areaCode, if (areaType == AreaType.DOMESTIC) "2" else "1")
        }
    }


    /**
     * 查询天气的重载方法，使用Int类型的areaType参数
     *
     * @param year     年份
     * @param month    月份
     * @param areaId   地区ID
     * @param areaType 地区类型 (1表示国内，2表示国际)
     * @return 天气数据列表
     */
    fun queryWeather(year: String, month: String, areaId: String, areaType: Int): MutableList<WeatherData?> {
        require(areaType == 1 || areaType == 2) { "areaType must be 1 (domestic) or 2 (international)" }
        return queryWeatherByAreaId(year, month, areaId, areaType.toString())
    }

    /**
     * 洛阳天气查询
     *
     * @param year  一年
     * @param month 月 入参
     * @return [List]<[WeatherData]>
     * @author zjarlin
     * @since 2023/04/10
     */
    fun queryWeather(year: String, month: String, areaId: String, areaType: String): MutableList<WeatherData?> {
        return queryWeatherByAreaId(year, month, areaId, areaType)
    }

    /**
     * 核心天气查询方法
     *
     * @param year     年份
     * @param month    月份
     * @param areaId   地区ID
     * @param areaType 地区类型
     * @return 天气数据列表
     */
    private fun queryWeatherByAreaId(year: String, month: String, areaId: String, areaType: String): MutableList<WeatherData?> {
        require(!isFutureDate(year.toInt(), month.toInt())) { "不能查询未来的天气!" }

        val url = "https://tianqi.2345.com/Pc/GetHistory"
        val cookie =
            "positionCityID=71778; positionCityPinyin=luolong; lastProvinceId=20; lastCityId=57073; Hm_lvt_a3f2879f6b3620a363bec646b7a8bcdd=1681045373; lastCountyId=57073; lastTownId=-1; lastTownTime=1681045444; lastCountyPinyin=luoyang; lastAreaName=æ´›é˜³; Hm_lpvt_a3f2879f6b3620a363bec646b7a8bcdd=1681045482; lastCountyTime=1681045481"
        val referer = "https://tianqi.2345.com/wea_history/57073.htm"

        val result = HttpUtil.createGet(url)
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .header("Referer", referer)
            .header("Cookie", cookie)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36"
            )
            .header("X-Requested-With", "XMLHttpRequest")
            .header("sec-ch-ua", "\"Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"macOS\"")
            .form("areaInfo[areaId]", areaId)
            .form("areaInfo[areaType]", areaType)
            .form("date[year]", year)
            .form("date[month]", month)
            .execute().body()

        val jsonObject = JSON.parseObject(result)
        val data = jsonObject.getString("data")
        if (data.contains("抱歉，暂无")) {
            error(data)
        }
        val parse = try {
            WeatherParser.parseHtml(data)
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }

        parse.forEach {
            it.apply {
                val date1 = it?.date ?: ""
                val splitDateAndWeekday = date1.splitDateAndWeekday()
                this?.date = splitDateAndWeekday.first
                this?.week = splitDateAndWeekday.second
                this?.areaId = areaId
                this?.areaType = areaType
            }
        }
        return parse
    }
}

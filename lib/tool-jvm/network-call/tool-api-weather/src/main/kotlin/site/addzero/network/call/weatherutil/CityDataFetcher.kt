package site.addzero.network.call.weatherutil

import cn.hutool.http.HttpUtil

/**
 * 城市数据获取工具类
 */
object CityDataFetcher {
    internal fun getCityData(): CityData {
        val fetchCitySelectData = fetchCitySelectData()
        val parseCityData = parseCityData(fetchCitySelectData)
        return parseCityData
    }

    fun searchCityData( keyword: String): List<FinalCityInfo> {
        val cityData = getCityData()
        val flatCityInfos = cityData.flatCityInfos
        val filter1 = flatCityInfos.filter {
            it.provinceCode.contains(keyword) || it.cityCode.contains(keyword)
        }
        val filter = filter1.map {
           FinalCityInfo(
               provinceId = it.cityId.toString(),
               cityId = it.cityShortName,
               provinceName = it.provinceCode.keepChinese(),
               cityName = it.cityCode.keepChinese()
           )
        }
        return filter
    }

    /**
     * 获取城市选择数据
     */
    private fun fetchCitySelectData(): String {
        val url = "https://tianqi.2345.com/tqpcimg/tianqiimg/theme4/js/citySelectData2.js"
        return HttpUtil.get(url)
    }

    /**
     * 解析城市数据
     */
    private fun parseCityData(rawData: String): CityData {
        // 提取 prov 和 provqx 数组
        val provRegex = Regex("""prov\[(\d+)]\s*=\s*'([^']+)""")
        val provqxRegex = Regex("""provqx\[(\d+)]\s*=\s*(\[.*?\])""")

        val provinces = mutableListOf<ProvinceInfo>()
        val provinceCities = mutableListOf<ProvinceCities>()
        val flatCityData = mutableListOf<FlatCityInfo>()

        // 解析省份数据
        val provinceMap = mutableMapOf<Int, ProvinceInfo>()
        provRegex.findAll(rawData).forEach { match ->
            val provId = match.groupValues[1].toInt()
            val provData = match.groupValues[2]
            val provinceInfo = parseProvinceInfo(provId, provData)
            provinces.add(provinceInfo)
            provinceMap[provId] = provinceInfo
        }

        // 解析城市数据
        provqxRegex.findAll(rawData).forEach { match ->
            val provId = match.groupValues[1].toInt()
            val citiesData = match.groupValues[2]
            // 解析数组字符串为城市信息列表
            val cities = parseProvinceCities(provId, citiesData)
            provinceCities.addAll(cities)

            // 构建拍平的数据
            cities.forEach { provinceCitiesGroup ->
                provinceCitiesGroup.cities.forEach { cityInfo ->
                    val provinceInfo = provinceMap[provinceCitiesGroup.provinceId]
                    if (provinceInfo != null) {
                        flatCityData.add(
                            FlatCityInfo(
                                provinceId = provinceInfo.id,
                                provinceCode = provinceInfo.code,
                                provinceName = provinceInfo.name,
                                provinceShortName = provinceInfo.shortName,
                                cityId = cityInfo.id,
                                cityCode = cityInfo.code,
                                cityName = cityInfo.name,
                                cityShortName = cityInfo.shortName
                            )
                        )
                    }
                }
            }
        }

        return CityData(provinces, provinceCities, flatCityData)
    }

    /**
     * 解析城市数组数据
     */
    private fun parseCitiesArray(arrayData: String): List<String> {
        // 移除方括号
        val content = arrayData.removeSurrounding("[", "]").trim()
        if (content.isEmpty()) return emptyList()

        // 按逗号分割，但要考虑数组内可能包含逗号的情况
        return if (content.startsWith("'") && content.endsWith("'")) {
            // 处理单个元素的情况
            listOf(content.trim('\''))
        } else if (content.contains("','")) {
            // 处理多个元素的情况
            content.split("','").map { it.trim('\'') }
        } else {
            // 处理单个元素但没有引号的情况
            listOf(content.trim('\''))
        }
    }

    /**
     * 解析省份信息
     */
    private fun parseProvinceInfo(provinceId: Int, provinceData: String): ProvinceInfo {
        // 格式: "12-B 北京-12"
        val parts = provinceData.split("-")
        return ProvinceInfo(
            id = provinceId,
            code = parts.getOrNull(1) ?: "",
            name = parts.getOrNull(0)?.split(" ")?.lastOrNull() ?: "",
            shortName = parts.getOrNull(0)?.split(" ")?.firstOrNull() ?: ""
        )
    }

    /**
     * 解析省份下的城市信息
     */
    private fun parseProvinceCities(provinceId: Int, citiesData: String): List<ProvinceCities> {
        // 格式: ['58321-H 合肥-58321|71873-B 包河-58321|...', '...']
        val cityGroups = parseCitiesArray(citiesData)
        return cityGroups.map { cityGroup ->
            val cities = parseCityInfo(cityGroup)
            ProvinceCities(provinceId, cities)
        }
    }

    /**
     * 解析城市信息
     */
    private fun parseCityInfo(cityData: String): List<CityInfo> {
        // 格式: "58321-H 合肥-58321|71873-B 包河-58321|..."
        if (cityData.isEmpty()) return emptyList()

        return cityData.split("|").mapNotNull { city ->
            val parts = city.split("-")
            if (parts.size >= 3) {
                CityInfo(
                    id = parts[2].toIntOrNull() ?: 0,
                    code = parts[1],
                    name = parts[0].split(" ").lastOrNull() ?: "",
                    shortName = parts[0].split(" ").firstOrNull() ?: ""
                )
            } else {
                null
            }
        }
    }
}

private fun String.keepChinese(): String {
    return this.replace(Regex("[^\\u4e00-\\u9fa5]"), "")
}

/**
 * 城市数据类
 * @param provinces 省份数据列表
 * @param provinceCities 省份下的城市数据列表
 * @param flatCityInfos 拍平的城市数据列表（类似left join效果）
 */
internal data class CityData(
    val provinces: List<ProvinceInfo>,
    val provinceCities: List<ProvinceCities>,
    val flatCityInfos: List<FlatCityInfo>
)

/**
 * 省份信息数据类
 * @param id 省份ID
 * @param code 编码
 * @param name 名称
 * @param shortName 简称
 */
data class ProvinceInfo(
    val id: Int,
    val code: String,
    val name: String,
    val shortName: String
)

/**
 * 省份下的城市列表
 * @param provinceId 省份ID
 * @param cities 城市列表
 */
internal data class ProvinceCities(
    val provinceId: Int,
    val cities: List<CityInfo>
)

/**
 * 城市信息数据类
 * @param id 城市ID
 * @param code 编码
 * @param name 名称
 * @param shortName 简称
 */
internal data class CityInfo(
    val id: Int,
    val code: String,
    val name: String,
    val shortName: String
)

/**
 * 拍平的城市信息数据类（类似left join效果）
 * @param provinceId 省份ID
 * @param provinceCode 省份编码
 * @param provinceName 省份名称
 * @param provinceShortName 省份简称
 * @param cityId 城市ID
 * @param cityCode 城市编码
 * @param cityName 城市名称
 * @param cityShortName 城市简称
 */
data class FlatCityInfo(
    val provinceId: Int,
    val provinceCode: String,
    val provinceName: String,
    val provinceShortName: String,
    val cityId: Int,
    val cityCode: String,
    val cityName: String,
    val cityShortName: String
)

data class FinalCityInfo(
    val provinceId: String,
    val cityId: String,
    val provinceName: String,
    val cityName: String,
)

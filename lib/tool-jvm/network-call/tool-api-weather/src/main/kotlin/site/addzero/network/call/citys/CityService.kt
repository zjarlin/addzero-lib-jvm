package site.addzero.network.call.citys

class CityService {
    private val cityRepository = CityRepository()
    private val internationalCityRepository = InternationalCityRepository()

    fun searchCities(keyword: String, areaType: AreaType = AreaType.DOMESTIC): List<Area> {
        return when (areaType) {
            AreaType.DOMESTIC -> cityRepository.searchCities(keyword)
            AreaType.INTERNATIONAL -> internationalCityRepository.searchCities(keyword)
        }
    }
    
    // 重载方法，使用Int类型的areaType参数
    fun searchCities(keyword: String, areaType: Int): List<Area> {
        return when (areaType) {
            1 -> cityRepository.searchCities(keyword)
            2 -> internationalCityRepository.searchCities(keyword)
            else -> throw IllegalArgumentException("areaType must be 1 (domestic) or 2 (international)")
        }
    }
    
    fun searchAllCities(keyword: String): List<Area> {
        val domesticCities = cityRepository.searchCities(keyword)
        val internationalCities = internationalCityRepository.searchCities(keyword)
        return domesticCities + internationalCities
    }
}
package site.addzero.network.call.citys

/**
 * 统一的区域实体类，用于表示国内城市和国际城市
 */
data class Area(
    val id: Long,
    val areaCode: String,
    val areaName: String,
    val cityName: String?,
    val provinceName: String?,
    val countryName: String?,
    val continents: String?
)

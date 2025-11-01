package site.addzero.network.call.citys

data class City(
    val id: Long,
    val areaId: String,
    val pinyin: String?,
    val py: String?,
    val areaName: String?,
    val cityName: String?,
    val provinceName: String?
)

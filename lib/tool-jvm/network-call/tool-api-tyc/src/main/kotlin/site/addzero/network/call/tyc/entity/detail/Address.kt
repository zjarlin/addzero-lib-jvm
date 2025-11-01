package site.addzero.network.call.tyc.entity.detail

/**
 * 地址信息实体类
 */
data class Address(
    /** 地址 */
    val address: String,
    /** 纬度 */
    val latitude: String,
    /** 经度 */
    val longitude: String,
    /** 报告年份 */
    val reportYear: String?,
    /** 来源显示 */
    val showSource: String,
    /** 来源显示权重 */
    val sourceDisplayWeight: Int
)

package site.addzero.network.call.tyc.entity.detail

/**
 * 办公室信息实体类
 */
data class Office(
    /** 地区 */
    val area: String,
    /** 公司ID */
    val cid: Long,
    /** 公司名称 */
    val companyName: String,
    /** 分数 */
    val score: Int,
    /** 状态 */
    val state: Any?,
    /** 总数 */
    val total: Int
)

package site.addzero.network.call.tyc.entity.search

/**
 * 搜索结果响应实体类
 */
data class SearchRes(
    /** 数据 */
    val `data`: CompanyData,
    /** 是否已登录 */
    val isLogin: Int, // 0
    /** 消息 */
    val message: String,
    /** 特殊信息 */
    val special: String, // 111
    /** 状态 */
    val state: String, // ok
    /** VIP消息 */
    val vipMessage: String
)

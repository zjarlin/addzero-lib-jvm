package site.addzero.network.call.tyc.entity.search

/**
 * 电话信息实体类
 */
data class PhoneInfo(
    /** 评论 */
    val comment: String, // 固定电话或非大陆号码
    /** 标签 */
    val label: String, // 2
    /** 号码 */
    val number: String, // 0379-65199909
    /** 来源 */
    val source: Any?, // null
    /** 类型 */
    val type: String // 9
)

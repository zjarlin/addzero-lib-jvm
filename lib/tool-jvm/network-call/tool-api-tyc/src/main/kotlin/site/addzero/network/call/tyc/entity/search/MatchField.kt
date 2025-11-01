package site.addzero.network.call.tyc.entity.search

/**
 * 匹配字段信息实体类
 */
data class MatchField(
    /** 内容 */
    val content: String, // 河南<em>中洛佳</em>科技
    /** 字段 */
    val `field`: String // 公司简称
)

package site.addzero.network.call.tianyancha.entity.detail

/**
 * 企业规模信息实体类
 */
data class EnterpriseScaleInfo(
    /** 说明文本 */
    val explainText: String,
    /** HTML格式说明文本 */
    val explainTextHtml: String,
    /** 图标类型 */
    val iconType: String,
    /** 企业规模 */
    val scale: String
)
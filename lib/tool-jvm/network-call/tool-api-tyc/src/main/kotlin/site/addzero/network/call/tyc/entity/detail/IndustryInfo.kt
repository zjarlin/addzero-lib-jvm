package site.addzero.network.call.tyc.entity.detail

/**
 * 行业信息实体类
 */
data class IndustryInfo(
    /** 行业代码 */
    val code: String,
    /** 说明文本 */
    val explainText: String,
    /** 一级行业名称 */
    val nameLevel1: String,
    /** 二级行业名称 */
    val nameLevel2: String,
    /** 三级行业名称 */
    val nameLevel3: String,
    /** 四级行业名称 */
    val nameLevel4: Any?
)

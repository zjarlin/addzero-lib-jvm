package site.addzero.network.call.tyc.entity.detail

/**
 * 标签信息V2实体类
 */
data class TagV2(
    /** 背景颜色 */
    val background: String,
    /** 点击超链接类型 */
    val clickHyperLinkType: Int,
    /** 点击URL */
    val clickUrl: String,
    /** 字体颜色 */
    val color: String,
    /** 悬停信息 */
    val hover: String,
    /** logo */
    val logo: String,
    /** 名称 */
    val name: String,
    /** 标签ID */
    val tagId: Int,
    /** 标题 */
    val title: String
)

package site.addzero.network.call.tyc.entity.detail

/**
 * 标签信息实体类
 */
data class Tag(
    /** 背景颜色 */
    val background: String,
    /** 标签信息 */
    val boxinfo: Boxinfo?,
    /** 字体颜色 */
    val color: String,
    /** 层级信息 */
    val layer: String,
    /** 层级信息数组 */
    val layerArray: List<String>?,
    /** 排序 */
    val sort: Int,
    /** 标题 */
    val title: String,
    /** 类型 */
    val type: Int,
    /** 值 */
    val value: String
)

package site.addzero.network.call.tyc.entity.detail

/**
 * 标签信息V3实体类
 */
data class TagV3(
    /** 动作类型 */
    val actionType: String,
    /** Android版本 */
    val androidVersion: Any?,
    /** 背景颜色 */
    val background: String,
    /** 边框颜色 */
    val borderColor: String,
    /** 边框透明度 */
    val borderTransparency: Double,
    /** 边框宽度 */
    val borderWidth: Double,
    /** 字体颜色 */
    val color: String,
    /** 字体族 */
    val fontFamily: String,
    /** 字体大小 */
    val fontSize: Int,
    /** 引导颜色 */
    val guideColor: String,
    /** 引导透明度 */
    val guideTransparency: Double,
    /** 悬停通知内容 */
    val hoverNoticeContent: String,
    /** 悬停通知类型 */
    val hoverNoticeType: Int,
    /** iOS版本 */
    val iOSVersion: Any?,
    /** ID */
    val id: Long,
    /** logo */
    val logo: String,
    /** 名称 */
    val name: String,
    /** 排序 */
    val order: Int,
    /** 弹出名称 */
    val popName: String,
    /** 标签点击超链接详情 */
    val profileTagClickHyperlinkDetails: String,
    /** 标签点击超链接类型 */
    val profileTagClickHyperlinkType: Int,
    /** 标签类型ID */
    val profileTagTypeId: Int,
    /** 标签类型排名 */
    val profileTagTypeRanking: Int,
    /** 路由动作 */
    val routingAction: Boolean,
    /** 路由地址 */
    val routingAddr: String?,
    /** 路由名称 */
    val routingName: String?,
    /** 显示条件 */
    val showCondition: Any?,
    /** 标题 */
    val title: String,
    /** 类型 */
    val type: Int
)

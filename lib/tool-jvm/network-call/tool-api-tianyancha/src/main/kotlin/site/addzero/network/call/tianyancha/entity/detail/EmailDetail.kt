package site.addzero.network.call.tianyancha.entity.detail

/**
 * 邮箱详情信息实体类
 */
data class EmailDetail(
    /** 邮箱 */
    val email: String,
    /** 报告年份 */
    val reportYear: String,
    /** 相同邮箱数量 */
    val sameEmailCount: String?,
    /** 来源显示 */
    val showSource: String,
    /** 来源显示权重 */
    val sourceDisplayWeight: String
)
package site.addzero.network.call.tyc.entity.detail

/**
 * 科技信息实体类
 */
data class ScienceTechnologyInfo(
    /** 超过百分比 */
    val exceedPercent: String,
    /** 等级 */
    val grade: String,
    /** 等级颜色 */
    val gradeColor: String,
    /** 分数 */
    val score: Int
)

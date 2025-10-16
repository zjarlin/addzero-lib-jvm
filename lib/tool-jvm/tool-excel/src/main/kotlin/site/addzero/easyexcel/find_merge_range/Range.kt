package site.addzero.easyexcel.find_merge_range

/**
 * @author zjarlin
 * @since 2023/11/14 13:31
 */
data class Range (
    val startRow: Int = 0,
    val endRow: Int = 0,
    val startCol: Int = 0,
    val endCol: Int = 0,
    val mergeType: String? = null
)

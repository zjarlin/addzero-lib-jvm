package site.addzero.network.call.tianyancha.entity.detail

/**
 * 公司员工信息实体类
 */
data class CompanyStaff(
    /** 分支机构数量 */
    val branchNum: Int,
    /** 是否存在分支机构 */
    val existBranch: Boolean,
    /** 说明文本 */
    val explainText: String,
    /** 员工数量 */
    val num: Int,
    /** 路由 */
    val route: String,
    /** 是否显示分支机构趋势图 */
    val showBranchTrendChart: Boolean,
    /** 是否显示趋势图 */
    val showTrendChart: Boolean,
    /** 来源 */
    val source: String,
    /** APP端来源显示 */
    val sourceForApp: String,
    /** 年份 */
    val year: Int
)
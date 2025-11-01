package site.addzero.network.call.tianyancha.entity.detail

data class StaffNumInfo(
    val branchNum: Int,
    val existBranch: Boolean,
    val explainText: String,
    val num: Int,
    val route: String,
    val showBranchTrendChart: Boolean,
    val showTrendChart: Boolean,
    val source: String,
    val sourceForApp: String,
    val year: Int
)

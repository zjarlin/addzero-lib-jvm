package site.addzero.network.call.tyc.entity.detail

/**
 * 员工数量信息列表实体类
 */
data class StaffNumInfoList(
    /** 公司员工列表 */
    val companyStaffs: List<CompanyStaff>,
    /** 说明文本 */
    val explainText: String,
    /** 是否弹出 */
    val isPop: Boolean
)

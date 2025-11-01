package site.addzero.network.call.tyc.entity.search

/**
 * 搜索结果数据实体类
 */
data class CompanyData(
    /** 建议查询 */
    val adviceQuery: Any?, // null
    /** 公司数量 */
    val companyCount: Int, // 4
    /** 公司人员数量 */
    val companyHumanCount: Int, // 0
    /** 公司列表 */
    val companyList: List<Company>,
    /** 公司总数 */
    val companyTotal: Int, // 4
    /** 公司总页数 */
    val companyTotalPage: Int, // 1
    /** 公司总数字符串 */
    val companyTotalStr: String, // 4
    /** 人员数量 */
    val humanCount: Int, // 0
    /** 修改后的查询 */
    val modifiedQuery: Any?, // null
    /** 搜索内容 */
    val searchContent: String // 中洛佳
)

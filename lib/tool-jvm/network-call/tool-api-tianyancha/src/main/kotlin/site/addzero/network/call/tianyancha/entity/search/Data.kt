package site.addzero.network.call.tianyancha.entity.search

data class Data(
    val adviceQuery: Any,
    val companyCount: Int,
    val companyHumanCount: Int,
    val companyList: List<Company>,
    val companyTotal: Int,
    val companyTotalPage: Int,
    val companyTotalStr: String,
    val humanCount: Int,
    val modifiedQuery: Any,
    val searchContent: String
)

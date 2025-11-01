package site.addzero.network.call.tyc.entity.search

/**
 * 联系人映射信息实体类
 */
data class ContantMap(
    /** 成立时间（长整型） */
    val establish_time_long: String, // 1576512000000
    /** 注册资本参数 */
    val param_reg_capital: String?, // 1000.0
    /** 回调来源 */
    val recallSrc: String // ES
)

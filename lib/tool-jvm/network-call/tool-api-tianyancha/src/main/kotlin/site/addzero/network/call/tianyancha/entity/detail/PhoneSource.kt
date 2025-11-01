package site.addzero.network.call.tianyancha.entity.detail

/**
 * 电话来源信息实体类
 */
data class PhoneSource(
    /** 公司ID */
    val cid: Int,
    /** 城市 */
    val city: String,
    /** 公司数量 */
    val companyCount: Int,
    /** 公司数量字符串 */
    val companyCountStr: String,
    /** 公司名称 */
    val companyName: String,
    /** 公司总数字符串 */
    val companyTotalStr: String,
    /** 公司类型 */
    val companyType: Int,
    /** GID */
    val gid: Long,
    /** 是否有更多公司 */
    val hasMoreCompany: Int,
    /** 原始电话号码 */
    val oriPhoneNumber: Any?,
    /** 电话号码 */
    val phoneNumber: String,
    /** 电话标签 */
    val phoneTag: Any?,
    /** 电话标签列表 */
    val phoneTagList: Any?,
    /** 电话标签类型 */
    val phoneTagType: Int,
    /** 电话提示 */
    val phoneTips: String,
    /** 电话类型 */
    val phoneType: Int,
    /** 省份 */
    val province: String,
    /** 报告年份 */
    val reportYear: String?,
    /** 来源显示 */
    val showSource: String,
    /** 疑似账户标签 */
    val suspectedAccountTag: Any?,
    /** 疑似账户标签URL */
    val suspectedAccountTagUrl: Any?
)
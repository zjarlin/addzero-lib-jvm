package site.addzero.network.call.tianyancha.entity

data class PhoneSource(
    val cid: Int,
    val city: String,
    val companyCount: Int,
    val companyCountStr: String,
    val companyName: String,
    val companyTotalStr: String,
    val companyType: Int,
    val gid: Long,
    val hasMoreCompany: Int,
    val oriPhoneNumber: Any?,
    val phoneNumber: String,
    val phoneTag: Any?,
    val phoneTagList: Any?,
    val phoneTagType: Int,
    val phoneTips: String,
    val phoneType: Int,
    val province: String,
    val reportYear: String?,
    val showSource: String,
    val suspectedAccountTag: Any?,
    val suspectedAccountTagUrl: Any?
)

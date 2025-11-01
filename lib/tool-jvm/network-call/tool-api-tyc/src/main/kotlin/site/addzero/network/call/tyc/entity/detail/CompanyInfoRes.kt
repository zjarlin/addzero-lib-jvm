package site.addzero.network.call.tyc.entity.detail

data class CompanyInfoRes(
    val `data`: CompanyDetailData,
    val errorCode: Any?,
    val errorMessage: Any?,
    val isLogin: Int?,
    val message: String?,
    val special: String?,
    val state: String?,
    val vipMessage: String?
)

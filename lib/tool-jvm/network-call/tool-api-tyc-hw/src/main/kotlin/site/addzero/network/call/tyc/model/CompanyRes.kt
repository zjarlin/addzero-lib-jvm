package site.addzero.network.call.tyc.model

data class CompanyRes(
    val code: Int, // 200
    val `data`: Data,
    val msg: String, // 成功
    val success: Boolean // true
)

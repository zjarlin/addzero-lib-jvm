package site.addzero.network.call.tianyancha.entity.search

data class SearchRes(
    val `data`: Data,
    val isLogin: Int,
    val message: String,
    val special: String,
    val state: String,
    val vipMessage: String
)

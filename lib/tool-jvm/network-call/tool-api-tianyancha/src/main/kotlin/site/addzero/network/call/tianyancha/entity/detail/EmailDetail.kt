package site.addzero.network.call.tianyancha.entity.detail

data class EmailDetail(
    val email: String,
    val reportYear: String,
    val sameEmailCount: String?,
    val showSource: String,
    val sourceDisplayWeight: String
)

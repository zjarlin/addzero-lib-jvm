package site.addzero.network.call.tianyancha.entity.detail

data class Address(
    val address: String,
    val latitude: String,
    val longitude: String,
    val reportYear: String?,
    val showSource: String,
    val sourceDisplayWeight: Int
)

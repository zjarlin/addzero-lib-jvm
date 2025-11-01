package site.addzero.network.call.tianyancha.entity

data class Tag(
    val background: String,
    val boxinfo: Boxinfo,
    val color: String,
    val layer: String,
    val layerArray: List<String>,
    val sort: Int,
    val title: String,
    val type: Int,
    val value: String
)

package site.addzero.entity.low_table


import kotlinx.serialization.json.JsonElement



data class TableSaveOrUpdateDTO(
    val tableName: String,
    val mutableMap: MutableMap<String, JsonElement>,
)

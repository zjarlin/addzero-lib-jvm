package site.addzero.apt.dict.model

/**
 * 字典模型
 * Dictionary model for system dictionary translation results
 */
data class DictModel(
    var dictCode: String,
    var value: String,
    var label: String
)
package site.addzero.apt.dict.trans.model.out

/**
 * 字典模型
 * Dictionary model for system dictionary translation results
 */
data class SystemDictModelResult(
    var dictCode: String,
    var value: String?=null,
    var label: String?
)

/**
 * 表字典模型
 */
data class TableDictModelResult(
    var tab: String,
    var codeColumnValue: String?,
    var nameColumnValue: String?
)
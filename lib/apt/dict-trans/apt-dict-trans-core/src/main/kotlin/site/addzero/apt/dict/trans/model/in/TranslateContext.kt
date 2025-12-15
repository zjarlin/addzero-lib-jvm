package site.addzero.apt.dict.trans.model.`in`

/**
 * Context for table dictionary translation
 * Used to collect all table translation requirements at compile time
 */
data class TableTranslateContext(
    var tab: String,
    var codeColumn: String,
    var nameColumn: String,
    val whereCondition: String? = null,
    val serializationAlias: String? = null,
)
data class DictTranslationContext(
    var dictCode: String,
    var value: String?=null,
    val serializationAlias: String? = null,
)

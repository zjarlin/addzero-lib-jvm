package site.addzero.util.db.wrapper.entity

data class WrapperContext(
    val customSqlSegment:String  , val  paramNameValuePairs:Map<String, Any>
)

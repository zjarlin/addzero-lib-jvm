package site.addzero.aop.dicttrans.dictaop.entity

data class NeedAddInfo(
    val rootObject: Any,

    val fieldName: String,

    /**
     * 需要递归创建
     */
    val recur: Boolean?,

    /**
     * 实体
     */
    val isT: Boolean?,

    /**
     * 集合
     */
    val isColl: Boolean?,

    val type: Class<*>,
)

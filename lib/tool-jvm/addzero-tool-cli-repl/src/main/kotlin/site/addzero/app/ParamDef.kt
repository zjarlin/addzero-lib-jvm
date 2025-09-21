package site.addzero.app

import kotlin.reflect.KType

/**
 * 参数定义类，描述单个参数的元信息
 * @param name 参数名称
 * @param type 参数类型(KType)
 * @param description 参数描述
 * @param defaultValue 默认值(可选)
 * @param isRequired 是否必填
 */
data class ParamDef(
    val name: String,
    val type: KType,
    val description: String,
    val defaultValue: Any? = null,
    val isRequired: Boolean = defaultValue == null
)



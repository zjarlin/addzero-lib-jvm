package site.addzero.example

import site.addzero.kcp.transformoverload.annotations.OverloadTransform
import site.addzero.kcp.transformoverload.annotations.TransformProvider

/**
 * 类型转换函数定义
 * 使用 @OverloadTransform 标记的扩展函数会被插件识别为转换器
 */

/**
 * Input 到实体的转换
 */
@OverloadTransform
fun <E : Any> Input<E>.toEntityInput(): E = toEntity()

/**
 * Draft 到实体的转换
 */
@OverloadTransform
fun <E : Any> Draft<E>.fromDraft(): E = toEntity()

/**
 * 可以在对象中组织转换函数
 */
object Converters : TransformProvider {
    @OverloadTransform
    fun <E : Any> Input<E>.toEntityViaConverters(): E = toEntity()
}

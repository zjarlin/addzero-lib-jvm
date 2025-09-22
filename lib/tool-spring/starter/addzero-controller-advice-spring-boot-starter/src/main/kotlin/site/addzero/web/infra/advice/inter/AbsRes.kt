package site.addzero.web.infra.advice.inter

import cn.hutool.core.util.TypeUtil
import java.lang.reflect.ParameterizedType

interface AbsRes<Res> {
    fun success(data: Any?): Res
    fun fail(data: Any?): Res


    fun CLASS(): Class<*> {
        val typeArgument = TypeUtil.getTypeArgument(this.javaClass, 0)
        // 处理泛型类型参数，如果typeArgument是ParameterizedType，则获取原始类型
        val clazz = if (typeArgument is ParameterizedType) {
            typeArgument.rawType as Class<*>
        } else {
            typeArgument as Class<*>
        }
        return clazz
    }

    fun support(): Boolean {
        val assignableFrom = this.javaClass.isAssignableFrom(CLASS())
        return assignableFrom
    }
}
package site.addzero.aop.dicttrans.inter

import cn.hutool.core.util.TypeUtil
import site.addzero.util.RefUtil
import java.lang.reflect.ParameterizedType

/**
 * @author zjarlin
 * @since 2023/11/8 10:31
 */
interface TransStrategy< T> {
    fun trans(t:  T): T
    fun support(t: Any): Boolean{
        val genericClass = RefUtil.getGenericClass(this, 0)
        return genericClass?.isAssignableFrom(t.javaClass) ?: false
    }

}

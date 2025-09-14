package site.addzero.aop.dicttrans.inter

import cn.hutool.core.util.TypeUtil

/**
 * @author zjarlin
 * @since 2023/11/8 10:31
 */
interface TransStrategy< T> {
    fun trans(t:  T): T
    fun support(t: Any): Boolean{
        val klass = TypeUtil.getTypeArgument(this.javaClass, 0) as Class<T>
        return klass.isAssignableFrom(t.javaClass)
    }

}

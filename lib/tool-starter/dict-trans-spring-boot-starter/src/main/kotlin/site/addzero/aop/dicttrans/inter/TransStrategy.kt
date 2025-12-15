package site.addzero.aop.dicttrans.inter

import site.addzero.util.RefUtil

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

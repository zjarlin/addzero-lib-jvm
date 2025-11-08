package site.addzero.web.infra.spring

import cn.hutool.extra.spring.SpringUtil
import org.springframework.core.ResolvableType

    inline fun <reified T> getGenericBean(vararg generics: Class<*>): T? {
    val bean = getBean(T::class.java, *generics)
    return bean
}
    /**
     * @param clazz Bean的类
     * @param generics 泛型类型数组
     * @param <T> Bean类型
     * @return Bean实例或null（如果不存在）
     */
    fun <T> getBean(clazz: Class<T>, vararg generics: Class<*>): T? {
        val applicationContext = SpringUtil.getApplicationContext()
        val resolvableType = ResolvableType.forClassWithGenerics(clazz, *generics)
        val beanProvider = applicationContext.getBeanProvider<T>(resolvableType)
        return beanProvider.getIfAvailable()
    }

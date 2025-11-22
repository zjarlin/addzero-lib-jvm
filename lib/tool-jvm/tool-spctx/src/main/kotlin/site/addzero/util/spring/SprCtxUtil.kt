@file:JvmName("SprCtxUtil")
package site.addzero.util.spring

import org.springframework.core.ResolvableType
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

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
    val applicationContext = SpringContextHolder.getApplicationContext()
    val resolvableType = ResolvableType.forClassWithGenerics(clazz, *generics)
    val beanProvider = applicationContext.getBeanProvider<T>(resolvableType)
    return beanProvider.getIfAvailable()
}

/**
 * 获取当前请求的ServletRequestAttributes
 * @return ServletRequestAttributes实例，如果在非Web环境中调用则返回null
 */
fun getCurrentRequestAttributes(): ServletRequestAttributes? {
    return RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
}


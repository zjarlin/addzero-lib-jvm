@file:JvmName("SprCtxUtil")

package site.addzero.util.spring

import org.springframework.core.ResolvableType
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import site.addzero.kcp.annotations.GenerateReified
import kotlin.reflect.KClass

class Some {
    /**

     * 简化版本 - 测试用，先不用 vararg
     */
    @GenerateReified
    fun <T : Any> getBeanSimple1(clazz: KClass<T>): T? {
        return null // TODO: Spring 集成，暂时返回 null
    }
}

/**
 *
 * Spring 工具类 - 使用 KCP 插件生成 reified 方法
 */
object SprCtxUtil {

    /**
     * 简化版本 - 测试用，先不用 vararg
     */

//    @GenerateReified
    fun <T> getBeanSimple(clazz: Class<T>): T? {
        return null // TODO: Spring 集成，暂时返回 null
    }
    /**
     * 简化版本 - 测试用，先不用 vararg
     */
    @GenerateReified("customGetBean")
    fun <T : Any,R> getBean(clazz: KClass<T>,clazz2: Class<R>): T? {
        return null
    }

    /**
     * @param clazz Bean的类
     * @param generics 泛型类型数组
     * @param <T> Bean类型
     * @return Bean实例或null（如果不存在）
     */
    @JvmStatic
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
    @JvmStatic
    fun getCurrentRequestAttributes(): ServletRequestAttributes? {
        return RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
    }
}


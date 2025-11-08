package site.addzero.web.infra.spring

import cn.hutool.extra.spring.SpringUtil
import org.springframework.core.ResolvableType

/**
 * @Description: spring上下文工具类
 * @author: jeecg-boot
 */
object SprCtxUtil {
    inline fun <reified T> getBean(vararg generics: Class<*>): T? {
        val applicationContext = SpringUtil.getApplicationContext()
        val resolvableType = ResolvableType.forClassWithGenerics(T::class.java, *generics)
        val beanProvider = applicationContext.getBeanProvider<T>(resolvableType)
        return beanProvider.getIfAvailable()
    }



}

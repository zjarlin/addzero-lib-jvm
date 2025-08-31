package com.addzero.web.infra.spring

import cn.hutool.extra.spring.SpringUtil
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.ResolvableType
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * @Description: spring上下文工具类
 * @author: jeecg-boot
 */
object SprCtxUtil {
    /**
     * 获取applicationContext
     *
     * @return
     */
    /**
     * 上下文对象实例
     */
    val applicationContext = SpringUtil.getApplicationContext()

    val httpServletRequest = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

    val httpServletResponse = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).response as HttpServletResponse


    val origin = httpServletRequest.getHeader("Origin")


    fun <T, S> getBean(clazz: Class<T>, vararg generics: Class<*>): S {
        val applicationContext = SpringUtil.getApplicationContext()
        val beanProvider =
            applicationContext.getBeanProvider<Any>(ResolvableType.forClassWithGenerics(clazz, *generics))
        val ifAvailable = beanProvider.getIfAvailable()
        return ifAvailable as S
    }


}

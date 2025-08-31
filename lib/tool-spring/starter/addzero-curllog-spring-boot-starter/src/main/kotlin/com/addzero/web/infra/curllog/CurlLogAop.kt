package com.addzero.web.infra.curllog

import io.gitee.zjarlin.com.addzero.rc.ScanControllerProperties
import jakarta.servlet.http.HttpServletRequest
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.Advisor
import org.springframework.aop.aspectj.AspectJExpressionPointcut
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class LogMethodInterceptor : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {
        // 在这里添加你的AOP逻辑

        val requestAttributes = RequestContextHolder.getRequestAttributes()
        val req = (requestAttributes as? ServletRequestAttributes)?.request
//        val req = SpringUtil.getBean(HttpServletRequest::class.java)
        val proceed = try {
            invocation.proceed()
        } catch (e: Exception) {
            val arguments = invocation.arguments
            dosth(req, arguments)
            throw e
        }
        return proceed
    }

}


class LogMethodInterceptorAWay : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {
        // 在这里添加你的AOP逻辑
      val requestAttributes = RequestContextHolder.getRequestAttributes()
        val req = (requestAttributes as? ServletRequestAttributes)?.request
        dosth(req, invocation.arguments)
        return invocation.proceed()
    }

}

private fun dosth(req: HttpServletRequest?, arguments: Array<Any>) {
    req.let {
        val curlCommand = CurlUtil.generateCurlCommand(it!!, arguments)
        val restUrl = it.requestURL.toString()
//        System.err.println("See Error restUrl: $restUrl")
        System.err.println("See Error Curl Command: $curlCommand")
    }
}


@Configuration
@EnableConfigurationProperties(ScanControllerProperties::class)
class CurlLogConfiguration {

    @Bean
    fun curlLogWhenErrorAdvisor(properties: ScanControllerProperties): Advisor {
        val pointcut = AspectJExpressionPointcut().apply {
            expression = properties.expression
        }
        return DefaultPointcutAdvisor(pointcut, LogMethodInterceptor())
    }

    @Bean
    fun curlLogAnywayAdvisor(): Advisor {
        val pointcut = AspectJExpressionPointcut().apply {
            expression = "@annotation(com.addzero.web.infra.curllog.CurlLog)"
        }
        return DefaultPointcutAdvisor(pointcut, LogMethodInterceptorAWay())
    }

}

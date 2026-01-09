package site.addzero.web.infra.curllog

import site.addzero.rc.ScanControllerProperties
//import javax.servlet.http.HttpServletRequest
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.Advisor
import org.springframework.aop.aspectj.AspectJExpressionPointcut
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest
import org.springframework.context.annotation.EnableAspectJAutoProxy
import site.addzero.rc.ExpressionScanAutoConfiguration

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


@AutoConfiguration(after = [ExpressionScanAutoConfiguration::class])
@Import(ExpressionScanAutoConfiguration::class)
@EnableAspectJAutoProxy
class CurlLogConfiguration {

    @Bean
    fun curlLogWhenErrorAdvisor(properties: ScanControllerProperties): Advisor {
        val pointcut = AspectJExpressionPointcut().apply {
            expression = properties.resolvedExpression()
        }
        return DefaultPointcutAdvisor(pointcut, LogMethodInterceptor())
    }

    @Bean
    fun curlLogAnywayAdvisor(): Advisor {
        val pointcut = AspectJExpressionPointcut().apply {
            expression = "@annotation(site.addzero.web.infra.curllog.CurlLog)"
        }
        return DefaultPointcutAdvisor(pointcut, LogMethodInterceptorAWay())
    }

}

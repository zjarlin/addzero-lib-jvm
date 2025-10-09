package site.addzero.aop.dicttrans.dictaop

import site.addzero.aop.dicttrans.strategy.StringStrategy
import site.addzero.aop.dicttrans.strategy.TransStrategySelector
import site.addzero.rc.AddzeroDictTransProperties
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.Advisor
import org.springframework.aop.Pointcut
import org.springframework.aop.aspectj.AspectJExpressionPointcut
import org.springframework.aop.support.ComposablePointcut
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationUtils
import site.addzero.aop.dicttrans.anno.Dict


@Configuration

class DictAopConfiguration {
    // 定义注解切点
    @Bean
    fun dictAnnotationPointcut(): AspectJExpressionPointcut {
        return AspectJExpressionPointcut().apply {
            // 拦截所有带 @Dict 注解的方法
            expression = "@annotation(site.addzero.aop.dicttrans.anno.Dict)"
        }
    }

    @Bean
    fun dictAdvisor(
        properties: AddzeroDictTransProperties,
        dictAnnotationPointcut: AspectJExpressionPointcut,
        transStrategySelector: TransStrategySelector
    ): Advisor {

        // 明确使用 Pointcut 构造 ComposablePointcut
        val expressionPointcut = AspectJExpressionPointcut().apply {
            expression = properties.expression
        }
        //       intersection() 表示&&

        val compositePointcut = ComposablePointcut(expressionPointcut as Pointcut)
            .union(dictAnnotationPointcut as Pointcut)


        val advice = object : MethodInterceptor {
            override fun invoke(invocation: MethodInvocation): Any? {

                // 获取方法上的 @Dict 注解（如果存在）
                val dict = AnnotationUtils.findAnnotation(
                    invocation.method,
                    Dict::class.java
                )

                val outVO = invocation.proceed()
                outVO ?: return outVO


                val strategy = transStrategySelector.getStrategy(outVO) ?: return outVO

                if (strategy is StringStrategy) {
                    strategy.dict = dict?:return outVO
                }
                val trans = strategy.trans(outVO)
                return trans


            }

        }

        return DefaultPointcutAdvisor(compositePointcut, advice)
    }


}

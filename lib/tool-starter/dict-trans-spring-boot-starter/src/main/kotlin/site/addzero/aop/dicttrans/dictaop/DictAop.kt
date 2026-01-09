package site.addzero.aop.dicttrans.dictaop

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.Advisor
import org.springframework.aop.aspectj.AspectJExpressionPointcut
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.*
import org.springframework.core.annotation.AnnotationUtils
import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.aop.dicttrans.strategy.StringStrategy
import site.addzero.aop.dicttrans.strategy.TransStrategySelector
import site.addzero.rc.ExpressionScanAutoConfiguration
import site.addzero.rc.ScanControllerProperties

@AutoConfiguration(after = [ExpressionScanAutoConfiguration::class])
@Import(ExpressionScanAutoConfiguration::class)
@EnableAspectJAutoProxy
@ComponentScan(basePackages = ["site.addzero.aop.dicttrans"])
class DictAopConfiguration {

    @Bean
    fun transStrategySelector(strategies: List<TransStrategy<*>>): TransStrategySelector =
        TransStrategySelector(strategies)

  @Bean
  fun dictAdvisor(
    scanControllerProperties: ScanControllerProperties,
    transStrategySelector: TransStrategySelector,
  ): Advisor {
    val combinedExpression =
      "@annotation(site.addzero.aop.dicttrans.anno.Dict) && (${scanControllerProperties.resolvedExpression()})"

    val pointcut = AspectJExpressionPointcut().apply {
      expression = combinedExpression
    }

    val advice = object : MethodInterceptor {
      override fun invoke(invocation: MethodInvocation): Any? {
        // 由于表达式中已经包含了@Dict注解检查，这里可以直接获取
        // 但为了向后兼容性和性能，仍然检查注解是否存在
        val dict = AnnotationUtils.findAnnotation(
          invocation.method,
          Dict::class.java
        ) ?: return invocation.proceed()

        val outVO = invocation.proceed()
        outVO ?: return outVO

        val strategy = transStrategySelector.getStrategy(outVO) ?: return outVO

        if (strategy is StringStrategy) {
          strategy.dict = dict
        }
        val trans = strategy.trans(outVO)
        return trans
      }
    }

    return DefaultPointcutAdvisor(pointcut, advice)
  }

}

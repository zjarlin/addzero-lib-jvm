package site.addzero.util.spring
import cn.hutool.extra.spring.SpringUtil
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext


object SpELUtils {
    fun evaluateExpression(expression: String?): Any? {
        val o = evaluateExpression<Any>(null, HashMap(), expression, null)
        return o
    }

    fun <T> evaluateExpression(expression: String?, resultType: Class<T>?): T? {
        val t = evaluateExpression<T>(null, HashMap(), expression, resultType)
        return t
    }

    fun <T> evaluateExpression(rootObject: Any, expression: String?, resultType: Class<T>?): T? {
        return evaluateExpression(rootObject, HashMap(), expression, resultType)
    }

    fun <T> evaluateExpression(variables: MutableMap<String, Any?>, expression: String?, resultType: Class<T>?): T? {
        return evaluateExpression(null, variables, expression, resultType)
    }

    fun <T> evaluateExpression(
        rootObject: Any?,
        variables: MutableMap<String, Any?>,
        expression: String??,
        resultType: Class<T>?
    ): T? {
        if (expression.isNullOrBlank()) {
            return null
        }
        val parser = SpelExpressionParser()
        val ctx = StandardEvaluationContext()
        ctx.setBeanResolver { _, beanName ->
            val bean = SpringUtil. getBean<Any>(beanName)
            bean
        }
        ctx.setVariables(variables)
        ctx.setRootObject(rootObject)
        val obj = parser.parseExpression(expression).getValue(ctx, resultType)
        return obj
    }
}

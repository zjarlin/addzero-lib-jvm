package site.addzero.web.infra.spring

import cn.hutool.core.text.CharSequenceUtil.isBlank
import cn.hutool.extra.spring.SpringUtil.getBean
import org.springframework.expression.BeanResolver
import org.springframework.expression.EvaluationContext
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext


object SpELUtils {
    fun evaluateExpression(expression: String): Any? {
        val o = evaluateExpression<Any>(null, HashMap(), expression, null)
        return o
    }

    fun <T> evaluateExpression(expression: String, resultType: Class<T>?): T? {
        val t = evaluateExpression<T>(null, HashMap(), expression, resultType)
        return t
    }

    fun <T> evaluateExpression(rootObject: Any, expression: String, resultType: Class<T>?): T? {
        return evaluateExpression<T>(rootObject, HashMap(), expression, resultType)
    }

    fun <T> evaluateExpression(variables: MutableMap<String, Any>, expression: String, resultType: Class<T>?): T? {
        return evaluateExpression(null, variables, expression, resultType)
    }

    fun <T> evaluateExpression(rootObject: Any?, variables: MutableMap<String, Any>, expression: String, resultType: Class<T>?): T? {
        if (isBlank(expression)) {
            return null
        }
        val parser: ExpressionParser = SpelExpressionParser()
        val ctx = StandardEvaluationContext()
        ctx.setBeanResolver(BeanResolver { context: EvaluationContext, beanName: String -> getBean(beanName) })
        ctx.setVariables(variables)
        ctx.setRootObject(rootObject)
        val obj = parser.parseExpression(expression).getValue(ctx, resultType)
        return obj
    }
}

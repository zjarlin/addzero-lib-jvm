package site.addzero.web.infra.advice

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import site.addzero.rc.AddzeroControllerAdviceProperties
import site.addzero.web.infra.advice.inter.AbsRes
import site.addzero.web.infra.advice.inter.SkipWrapperCheck

/**
 * 包装Controller结果, 如果已是Result类型, 则直接返回, 否则进行包装.
 *
 */
@RestControllerAdvice
class ResponseAdvice(
    private val addzeroControllerAdviceProperties: AddzeroControllerAdviceProperties,
    private val objectMapper: ObjectMapper,
    private val absRes: AbsRes<*>,
    private val skipWrapperCheck: SkipWrapperCheck,
) : ResponseBodyAdvice<Any?> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean {
        val containingClass = returnType.containingClass

        val pkg = containingClass.`package`.name

        // 检查是否在包含列表中
        val isIncluded = addzeroControllerAdviceProperties.includePackages.any {
            pkg.startsWith(it)
        }

        if (isIncluded) {
            // 检查是否在黑名单中
            val isExcludedByBlackList = addzeroControllerAdviceProperties.excludePackages.any {
                pkg.startsWith(it)
            }

            // 排除标记了注解或者在排除列表中的类，以及在黑名单中的包
            return !isExcludedByBlackList && !returnType.hasMethodAnnotation(IgnoreResponseAdvice::class.java)
        }

        return false
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        return when {
            body == null -> {
                absRes.success("")
            }

            isWrappedResponse(body) || skipWrapperCheck.shouldSkip(body) -> {
                body
            }

            body is String -> {
                try {
                    val r = objectMapper.readValue(body, AbsRes::class.java)
                    objectMapper.writeValueAsString(r)
                } catch (e: Exception) {
                    val success = absRes.success(body)
                    objectMapper.writeValueAsString(success)
                }
            }

            else -> {
                absRes.success(body)
            }
        }
    }

    private fun isWrappedResponse(body: Any): Boolean {
        return try {
            absRes.CLASS().isAssignableFrom(body.javaClass)
        } catch (e: Exception) {
            // 如果获取CLASS()时发生异常，默认认为不是包装的响应
            false
        }
    }
}

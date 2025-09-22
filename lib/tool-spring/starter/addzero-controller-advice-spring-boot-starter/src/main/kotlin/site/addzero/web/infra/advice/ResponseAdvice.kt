package site.addzero.web.infra.advice

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import site.addzero.web.infra.advice.inter.AbsRes

/**
 * 包装Controller结果, 如果已是Result类型, 则直接返回, 否则进行包装.
 *
 */
@RestControllerAdvice
class ResponseAdvice(
    @field:Value("\${controller.advice.path}") private val path: List<String?>,
    private val objectMapper: ObjectMapper,
    private val absRes: AbsRes<*>,
) : ResponseBodyAdvice<Any?> {

//    private val wrapperConfig: ResponseWrapperConfig = responseWrapperConfig ?: DefaultResponseWrapperConfig()

    companion object {
        private val EXCLUDED_CONTROLLER_CLASSES: Set<Class<*>> = emptySet()
        private val EXCLUDED_CONTROLLER_STRING: Set<String> = emptySet()
    }

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean {
        val containingClass = returnType.containingClass

        val pkg = containingClass.`package`.name

        if (path.isEmpty() || path.none {
                it != null && pkg.startsWith(it)
            }

        ) {
            return false
        }

        // 排除标记了注解或者在排除列表中的类
        return !returnType.hasMethodAnnotation(IgnoreResponseAdvice::class.java) && !EXCLUDED_CONTROLLER_CLASSES.contains(
            containingClass
        ) && !EXCLUDED_CONTROLLER_STRING.contains(containingClass.name)

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

            isWrappedResponse(body) || body is JSONArray -> {
                body
            }

            body is JSONObject -> {
                if (body.containsKey("code") && body.containsKey("message")) body else absRes.success(body)
            }

            body is String -> {
                try {
                    val r = JSON.parseObject(body, AbsRes::class.java)
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

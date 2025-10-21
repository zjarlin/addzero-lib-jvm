package site.addzero.web.infra.advice.inter

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

/**
 * Interface for checking whether a response body should skip wrapping.
 * Users can implement this interface to customize the logic.
 */
interface SkipWrapperCheck {

    /**
     * Check if the given body should skip wrapping
     * @param body the response body
     * @return true if the body should skip wrapping, false otherwise
     */
    fun shouldSkip(body: Any?): Boolean
}

/**
 * Default implementation of SkipWrapperCheck.
 * This implementation provides a basic check for common JSON array and object types.
 */
@Component
@ConditionalOnMissingBean(SkipWrapperCheck::class)
class DefaultSkipWrapperCheck : SkipWrapperCheck {

    /**
     * Check if the given body should skip wrapping.
     * By default, we check for common JSON structures that should not be wrapped.
     * Users can provide their own implementation for custom logic.
     * @param body the response body
     * @return true if the body should skip wrapping, false otherwise
     */
    override fun shouldSkip(body: Any?): Boolean {
        // 用户可以自定义实现来决定是否跳过包装
        // 这里提供一个基础实现，用户可以覆盖此行为
        return false
    }
}

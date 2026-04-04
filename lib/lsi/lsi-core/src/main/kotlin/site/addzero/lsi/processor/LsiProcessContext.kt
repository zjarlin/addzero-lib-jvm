package site.addzero.lsi.processor

import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.environment.LsiEnvironment
import site.addzero.lsi.logger.LsiLogger

/**
 * 面向 LSI 类集合处理流程的通用上下文。
 */
data class LsiProcessContext(
    val environment: LsiEnvironment,
    val classes: Set<LsiClass>
) {
    val logger
        get() = requireNotNull(environment.logger) { "LsiEnvironment.logger 不能为空" }

    val options
        get() = environment.options.orEmpty()

    val entities
        get() = classes
}

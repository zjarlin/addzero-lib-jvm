package site.addzero.valid.valid_ex.key

import javax.validation.Constraint
import kotlin.reflect.KClass

/**
 * 标记在类上或字段上，用于标识唯一性校验规则
 * 当标记在类上时，表示该类需要进行唯一性校验
 * 当标记在字段上时，表示该字段参与唯一性校验
 *
 * @property message 错误消息
 * @property group 分组，默认为空字符串，表示默认分组
 * @property validator 唯一性校验器实现类
 * @author zjarlin
 * @since 2025/10/22
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [KeyValidator::class])
annotation class Key(
    val message: String = "字段组合已存在",
    val group: String = "",
    val validator: KClass<out KeyUniqueValidator> = JdbcKeyUniqueValidator::class,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)
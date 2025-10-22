package site.addzero.valid.valid_ex.key

import javax.validation.Constraint
import kotlin.reflect.KClass

/**
 * 标记在字段上，用于标识该字段参与唯一性校验
 *
 * @property group 分组，默认为空字符串，表示默认分组
 * @property validator 唯一性校验器实现类
 * @author zjarlin
 * @since 2025/10/22
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [KeyValidator::class])
annotation class Key(
    val group: String = "",
    val validator: KClass<out KeyUniqueValidator> = JdbcKeyUniqueValidator::class
)
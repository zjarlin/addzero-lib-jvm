package site.addzero.valid.valid_ex.custom

import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * @author zjarlin
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(
    AnnotationRetention.RUNTIME
)
@JvmRepeatable(
    CustomValids::class
)
annotation class CustomValid(
    val checkNull: Boolean = false,
    val useSpel: Boolean = false,
    val expression: String = "",
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

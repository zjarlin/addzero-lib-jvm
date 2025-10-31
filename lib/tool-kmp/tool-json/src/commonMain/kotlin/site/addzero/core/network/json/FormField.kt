@file:OptIn(ExperimentalSerializationApi::class)

package site.addzero.core.network.json

import kotlinx.serialization.ExperimentalSerializationApi


sealed class FormField<out T> {

    object Undefined : FormField<Nothing>()
    class Null<T> : FormField<T>()
    data class Value<T>(val data: T) : FormField<T>()

    companion object {
        fun <T> T?.undefined(): FormField<T> = Undefined
        fun <T> T?.toFormField(): FormField<T> = when (this) {
            null -> Null()
            else -> Value(this)
        }
    }
}

val <T> FormField<T>.value: T?
    get() = when (this) {
        is FormField.Value -> this.data
        is FormField.Null -> null
        FormField.Undefined -> null
    }

fun <T> FormField<T>.valueOr(default: T): T = when (this) {
    is FormField.Value -> this.data
    else -> default
}

fun <T> FormField<T>.valueOrNull(): T? = when (this) {
    is FormField.Value -> this.data
    else -> null
}

// 更具描述性的扩展方法命名
fun <T> FormField<T>.orDefault(default: T): T = valueOr(default)
fun <T> FormField<T>.orNull(): T? = valueOrNull()

// 用于嵌套 FormField 的处理
fun <T> FormField<FormField<T>>.flatten(): FormField<T> = when (this) {
    is FormField.Value -> this.data
    is FormField.Null -> FormField.Null()
    FormField.Undefined -> FormField.Undefined
}

// 用于处理嵌套结构的映射
inline fun <T, R> FormField<T>.map(transform: (T) -> R): FormField<R> = when (this) {
    is FormField.Value -> FormField.Value(transform(this.data))
    is FormField.Null -> FormField.Null()
    FormField.Undefined -> FormField.Undefined
}

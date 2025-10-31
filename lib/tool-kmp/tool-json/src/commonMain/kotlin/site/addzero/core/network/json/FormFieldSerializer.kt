@file:OptIn(ExperimentalSerializationApi::class)

package site.addzero.core.network.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import site.addzero.core.network.json.FormField.Companion.toFormField

class FormFieldSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<FormField<T>> {
    override val descriptor = dataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: FormField<T>) {
        when (value) {
            is FormField.Value -> encoder.encodeSerializableValue(dataSerializer, value.data)
            is FormField.Null -> encoder.encodeNull()
            FormField.Undefined -> {}
        }
    }

    override fun deserialize(decoder: Decoder): FormField<T> {
        val decodeNullableSerializableValue = decoder.decodeNullableSerializableValue(dataSerializer)
        return decodeNullableSerializableValue.toFormField()
    }

    companion object {
        inline operator fun <reified T> invoke(): FormFieldSerializer<T> {
            return FormFieldSerializer(serializer())
        }
    }
}

// 为嵌套 FormField 提供便利的序列化器构建方法
inline fun <reified T> formFieldSerializer(): FormFieldSerializer<T> {
    return FormFieldSerializer(serializer())
}

// 为嵌套 FormField 提供便利的序列化器构建方法
inline fun <reified T> nestedFormFieldSerializer(): FormFieldSerializer<FormField<T>> {
    return FormFieldSerializer(formFieldSerializer<T>())
}

// 处理多态类型的辅助函数
inline fun <reified T : Any> polymorphicFormFieldSerializer(): FormFieldSerializer<T> {
    return FormFieldSerializer(serializer<T>())
}

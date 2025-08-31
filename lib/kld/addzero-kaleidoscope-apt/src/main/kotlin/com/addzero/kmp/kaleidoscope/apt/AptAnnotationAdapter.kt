package com.addzero.kmp.kaleidoscope.apt

import com.addzero.kmp.kaleidoscope.core.KldAnnotation
import com.addzero.kmp.kaleidoscope.core.KldAnnotationArgument
import com.addzero.kmp.kaleidoscope.core.KldAnnotationValue
import com.addzero.kmp.kaleidoscope.core.KldType
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * 将AnnotationMirror转换为KldAnnotation
 */
fun AnnotationMirror.toKldAnnotation(): KldAnnotation {
    val mirror = this
    return object : KldAnnotation {
        override val annotationType= mirror.annotationType.toKldType()
        override val simpleName= (mirror.annotationType.asElement() as TypeElement).simpleName.toString()
        override val qualifiedName= (mirror.annotationType.asElement() as TypeElement).qualifiedName?.toString()
        override val arguments by lazy {
            mirror.elementValues.mapKeys { entry ->
                entry.key.simpleName.toString()
            }.mapValues { entry ->
                entry.value.toAnnotationValue()
            }
        }
        override val argumentList by lazy {
            arguments.map { (name, value) ->
                KldAnnotationArgument(name, value)
            }
        }

        override fun getArgument(name: String): KldAnnotationValue? {
            return arguments[name]
        }

        override fun getArgumentOrDefault(name: String, defaultValue: KldAnnotationValue): KldAnnotationValue {
            return arguments[name] ?: defaultValue
        }

        override fun hasArgument(name: String): Boolean {
            return arguments.containsKey(name)
        }
    }
}

/**
 * 将AnnotationValue转换为AnnotationValue
 */
fun AnnotationValue.toAnnotationValue(): KldAnnotationValue {
    val value = this
    return when (val actualValue = value.value) {
        is String -> KldAnnotationValue.StringValueKld(
            actualValue,
            createStringType()
        )

        is Boolean, is Byte, is Short, is Int, is Long, is Float, is Double, is Char ->
            KldAnnotationValue.Primitive(
                actualValue,
                createStringType()
            )

        is VariableElement -> {
            // 枚举值
            KldAnnotationValue.KldEnumValue(
                actualValue.simpleName.toString(),
                actualValue.asType().toKldType(),
                actualValue.enclosingElement.asType().toKldType(),
                actualValue.toKldElement()
            )
        }

        is TypeMirror -> {
            // 类型值
            val typeElement = (actualValue as javax.lang.model.type.DeclaredType).asElement() as TypeElement
            KldAnnotationValue.KldClassValue(
                actualValue.toKldType(),
                typeElement.qualifiedName?.toString() ?: "",
                typeElement.simpleName.toString()
            )
        }

        is AnnotationMirror -> {
            // 嵌套注解
            KldAnnotationValue.KldAnnotationInstance(
                actualValue.toKldAnnotation(),
                actualValue.annotationType.toKldType()
            )
        }

        is List<*> -> {
            // 数组值
            KldAnnotationValue.KldArrayValue(
                actualValue.mapNotNull { it as? AnnotationValue }
                    .map { it.toAnnotationValue() },
                createStringType()
            )
        }

        else -> KldAnnotationValue.KldUnknown(
            actualValue,
            createStringType()
        )
    }
}


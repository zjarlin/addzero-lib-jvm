package com.addzero.kaleidoscope.core

/**
 * 注解抽象接口
 *
 * 统一表示 APT 和 KSP 中的注解信息
 */
interface KldAnnotation {

    /**
     * 注解类型
     */
    val annotationType: KldType

    /**
     * 注解的简单名称
     */
    val simpleName: String

    /**
     * 注解的完全限定名称
     */
    val qualifiedName: String?

    /**
     * 注解参数映射
     */
    val arguments: Map<String, KldAnnotationValue>

    /**
     * 注解参数列表（保持顺序）
     */
    val argumentList: List<KldAnnotationArgument>

    /**
     * 获取指定名称的参数值
     */
    fun getArgument(name: String): KldAnnotationValue?

    /**
     * 获取指定名称的参数值，如果不存在则返回默认值
     */
    fun getArgumentOrDefault(name: String, defaultValue: KldAnnotationValue): KldAnnotationValue

    /**
     * 检查是否有指定名称的参数
     */
    fun hasArgument(name: String): Boolean
}

/**
 * 注解参数
 */
data class KldAnnotationArgument(
    /**
     * 参数名称
     */
    val name: String,

    /**
     * 参数值
     */
    val value: KldAnnotationValue
)

/**
 * 注解值抽象接口
 *
 * 表示注解参数的值，可以是原始类型、字符串、枚举、类、注解、数组等
 */
sealed class KldAnnotationValue {

    /**
     * 获取实际值
     */
    abstract val value: Any?

    /**
     * 值的类型
     */
    abstract val type: KldType

    /**
     * 原始类型值
     */
    data class Primitive(override val value: Any, override val type: KldType) : KldAnnotationValue()

    /**
     * 字符串值
     */
    data class StringValueKld(override val value: String, override val type: KldType) : KldAnnotationValue()

    /**
     * 枚举值
     */
    data class KldEnumValue(
        override val value: String,
        override val type: KldType,
        val enumClass: KldType,
        val enumConstant: KldElement
    ) : KldAnnotationValue()

    /**
     * 类值 - 使用全限定类名字符串表示，避免依赖JVM字节码Class
     */
    data class KldClassValue(
        override val type: KldType,
        /**
         * 类的全限定名称
         */
        val qualifiedName: String,
        /**
         * 类的简单名称
         */
        val simpleName: String
    ) : KldAnnotationValue() {
        override val value: String get() = qualifiedName
    }

    /**
     * 注解值
     */
    data class KldAnnotationInstance(override val value: KldAnnotation, override val type: KldType) : KldAnnotationValue()

    /**
     * 数组值
     */
    data class KldArrayValue(
        override val value: List<KldAnnotationValue>,
        override val type: KldType
    ) : KldAnnotationValue()

    /**
     * 未知值
     */
    data class KldUnknown(override val value: Any?, override val type: KldType) : KldAnnotationValue()
}


package com.addzero.kaleidoscope.core

/**
 * 类型抽象接口
 *
 * 统一表示 APT 和 KSP 中的类型信息
 */
interface KldType {

    /**
     * 类型名称
     */
    val typeName: String

    /**
     * 完全限定名称
     */
    val qualifiedName: String?

    /**
     * 是否可空
     */
    val isNullable: Boolean

    /**
     * 是否为原始类型
     */
    val isPrimitive: Boolean

    /**
     * 是否为数组类型
     */
    val isArray: Boolean

    /**
     * 是否为泛型类型
     */
    val isGeneric: Boolean

    /**
     * 是否为通配符类型
     */
    val isWildcard: Boolean

    /**
     * 是否为类型变量
     */
    val isTypeVariable: Boolean

    /**
     * 类型种类
     */
    val kldTypeKind: KldTypeKind

    /**
     * 类型参数列表
     */
    val typeArguments: List<KldType>

    /**
     * 上界类型列表（泛型约束）
     */
    val upperBounds: List<KldType>

    /**
     * 下界类型列表
     */
    val lowerBounds: List<KldType>

    /**
     * 对应的类型元素（如果是已声明类型）
     */
    val declaration: KldElement?

    /**
     * 类型注解
     */
    val annotations: List<KldAnnotation>

    /**
     * 获取擦除后的类型
     */
    fun getErasedType(): KldType

    /**
     * 检查是否可以赋值给指定类型
     */
    fun isAssignableFrom(other: KldType): Boolean

    /**
     * 检查是否可以赋值给指定类型
     */
    fun isAssignableTo(other: KldType): Boolean

    /**
     * 获取原始类型（如果是包装类型）
     */
    fun getPrimitiveType(): KldType?

    /**
     * 获取包装类型（如果是原始类型）
     */
    fun getWrapperType(): KldType?

    /**
     * 获取数组元素类型
     */
    fun getArrayElementType(): KldType?

    /**
     * 替换类型参数
     */
    fun substitute(substitutions: Map<String, KldType>): KldType
}

/**
 * 类型种类枚举
 */
enum class KldTypeKind {
    /** 布尔类型 */
    BOOLEAN,

    /** 字节类型 */
    BYTE,

    /** 短整型 */
    SHORT,

    /** 整型 */
    INT,

    /** 长整型 */
    LONG,

    /** 字符类型 */
    CHAR,

    /** 浮点型 */
    FLOAT,

    /** 双精度浮点型 */
    DOUBLE,

    /** 空类型 */
    VOID,

    /** 空类型（Kotlin Unit） */
    UNIT,

    /** 未知类型 */
    NONE,

    /** 空值类型 */
    NULL,

    /** 数组类型 */
    ARRAY,

    /** 已声明类型（类、接口、枚举等） */
    DECLARED,

    /** 错误类型 */
    ERROR,

    /** 类型变量 */
    TYPE_VARIABLE,

    /** 通配符类型 */
    WILDCARD,

    /** 可执行类型（方法、构造函数） */
    EXECUTABLE,

    /** 包类型 */
    PACKAGE,

    /** 联合类型 */
    UNION,

    /** 交集类型 */
    INTERSECTION,

    /** 模块类型 */
    MODULE,

    /** 其他类型 */
    OTHER
}

/**
 * 泛型类型参数接口
 */
interface KldTypeParameter : KldElement {

    /**
     * 类型参数名称
     */
    val name: String

    /**
     * 边界类型列表
     */
    val bounds: List<KldType>

    /**
     * 变异性（协变、逆变、不变）
     */
    val kldVariance: KldVariance

    /**
     * 是否为具体化类型参数（Kotlin reified）
     */
    val isReified: Boolean
}

/**
 * 变异性枚举
 */
enum class KldVariance {
    /** 不变 */
    INVARIANT,

    /** 协变 */
    COVARIANT,

    /** 逆变 */
    CONTRAVARIANT
}

/**
 * 函数类型接口
 */
interface KldFunctionType : KldType {

    /**
     * 参数类型列表
     */
    val parameterTypes: List<KldType>

    /**
     * 返回类型
     */
    val returnType: KldType

    /**
     * 是否为挂起函数
     */
    val isSuspend: Boolean

    /**
     * 接收者类型
     */
    val receiverType: KldType?
}

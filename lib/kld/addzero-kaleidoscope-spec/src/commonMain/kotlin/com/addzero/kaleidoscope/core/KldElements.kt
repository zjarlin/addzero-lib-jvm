package com.addzero.kaleidoscope.core

/**
 * 包元素接口
 */
interface KldPackageElement : KldElement {

    /**
     * 是否为未命名包
     */
    val isUnnamed: Boolean

    /**
     * 获取子包列表
     */
    fun getSubpackages(): List<KldPackageElement>

    /**
     * 获取包中的类型列表
     */
    fun getTypes(): List<KldTypeElement>
}

/**
 * 类型元素接口（类、接口、枚举、注解等）
 */
interface KldTypeElement : KldElement {

    /**
     * 完全限定名称
     */
    override val qualifiedName: String

    /**
     * 类型种类
     */
    val typeKind: KldTypeElementKind

    /**
     * 父类型
     */
    val superclass: KldType?

    /**
     * 实现的接口列表
     */
    val interfaces: List<KldType>

    /**
     * 类型参数列表
     */
    val kldTypeParameters: List<KldTypeParameter>

    /**
     * 嵌套类型列表
     */
    val nestedTypes: List<KldTypeElement>

    /**
     * 字段列表
     */
    val fields: List<KldVariableElement>

    /**
     * 方法列表
     */
    val methods: List<KldExecutableElement>

    /**
     * 构造函数列表
     */
    val constructors: List<KldExecutableElement>

    /**
     * 属性列表（Kotlin特有）
     */
    val properties: List<KldPropertyElement>

    /**
     * 伴生对象（Kotlin特有）
     */
    val companionObject: KldTypeElement?

    /**
     * 是否为内部类
     */
    val isInner: Boolean

    /**
     * 是否为本地类
     */
    val isLocal: Boolean

    /**
     * 是否为匿名类
     */
    val isAnonymous: Boolean

    /**
     * 获取指定名称的方法
     */
    fun getMethod(name: String, parameterTypes: List<KldType>): KldExecutableElement?

    /**
     * 获取指定名称的所有方法
     */
    fun getMethods(name: String): List<KldExecutableElement>

    /**
     * 获取指定名称的字段
     */
    fun getField(name: String): KldVariableElement?

    /**
     * 获取指定名称的属性
     */
    fun getProperty(name: String): KldPropertyElement?

    /**
     * 获取默认构造函数
     */
    fun getDefaultConstructor(): KldExecutableElement?
}

/**
 * 类型元素种类
 */
enum class KldTypeElementKind {
    CLASS,
    INTERFACE,
    ENUM,
    ANNOTATION_TYPE,
    RECORD,
    OBJECT,
    COMPANION_OBJECT,
    ENUM_ENTRY
}

/**
 * 可执行元素接口（方法、构造函数）
 */
interface KldExecutableElement : KldElement {

    /**
     * 返回类型
     */
    val returnType: KldType

    /**
     * 参数列表
     */
    val parameters: List<KldVariableElement>

    /**
     * 类型参数列表
     */
    val kldTypeParameters: List<KldTypeParameter>

    /**
     * 抛出的异常类型列表
     */
    val thrownTypes: List<KldType>

    /**
     * 接收者类型（Kotlin扩展函数）
     */
    val receiverType: KldType?

    /**
     * 是否为变长参数方法
     */
    val isVarArgs: Boolean

    /**
     * 是否为抽象方法
     */
    val isAbstract: Boolean

    /**
     * 是否为默认方法
     */
    val isDefault: Boolean

    /**
     * 是否为挂起函数（Kotlin特有）
     */
    val isSuspend: Boolean

    /**
     * 是否为内联函数（Kotlin特有）
     */
    val isInline: Boolean

    /**
     * 是否为运算符重载（Kotlin特有）
     */
    val isOperator: Boolean

    /**
     * 是否为中缀函数（Kotlin特有）
     */
    val isInfix: Boolean

    /**
     * 方法签名
     */
    val signature: String
}

/**
 * 变量元素接口（字段、参数、局部变量等）
 */
interface KldVariableElement : KldElement {

    /**
     * 变量类型
     */
    val type: KldType

    /**
     * 常量值（如果是编译时常量）
     */
    val constantValue: Any?

    /**
     * 是否为编译时常量
     */
    val isConstant: Boolean

    /**
     * 是否为可变变量（Kotlin var）
     */
    val isMutable: Boolean

    /**
     * 是否为lateinit变量（Kotlin特有）
     */
    val isLateinit: Boolean

    /**
     * 变量种类
     */
    val kldVariableKind: KldVariableKind
}

/**
 * 变量种类
 */
enum class KldVariableKind {
    FIELD,
    PARAMETER,
    LOCAL_VARIABLE,
    EXCEPTION_PARAMETER,
    RESOURCE_VARIABLE,
    ENUM_CONSTANT,
    BINDING_VARIABLE
}

/**
 * 属性元素接口（Kotlin特有）
 */
interface KldPropertyElement : KldElement {

    /**
     * 属性类型
     */
    val type: KldType

    /**
     * Getter方法
     */
    val getter: KldExecutableElement?

    /**
     * Setter方法
     */
    val setter: KldExecutableElement?

    /**
     * 后备字段
     */
    val backingField: KldVariableElement?

    /**
     * 是否为可变属性
     */
    val isMutable: Boolean

    /**
     * 是否为lateinit属性
     */
    val isLateinit: Boolean

    /**
     * 是否为const属性
     */
    val isConst: Boolean

    /**
     * 是否有自定义getter
     */
    val hasCustomGetter: Boolean

    /**
     * 是否有自定义setter
     */
    val hasCustomSetter: Boolean

    /**
     * 委托表达式（如果使用委托）
     */
    val delegateExpression: String?
}

// 具体的类型实现接口

/**
 * 原始类型
 */
interface KldPrimitiveType : KldType

/**
 * 无类型（void, Unit等）
 */
interface KldNoType : KldType

/**
 * 数组类型
 */
interface KldArrayType : KldType {
    /**
     * 数组元素类型
     */
    val kldComponentType: KldType
}

/**
 * 已声明类型（类、接口等）
 */
interface KldDeclaredType : KldType {
    /**
     * 类型声明元素
     */
    val kldTypeElement: KldTypeElement

    /**
     * 外部类型（对于内部类）
     */
    val kldEnclosingType: KldDeclaredType?
}

/**
 * 错误类型
 */
interface KldErrorType : KldType

/**
 * 类型变量
 */
interface KldTypeVariableType : KldType {
    /**
     * 类型参数元素
     */
    val kldTypeParameter: KldTypeParameter
}

/**
 * 通配符类型
 */
interface KldWildcardType : KldType {
    /**
     * 上界类型
     */
    val extendsBound: KldType?

    /**
     * 下界类型
     */
    val superBound: KldType?
}

/**
 * 可执行类型（方法类型）
 */
interface KldExecutableType : KldType {
    /**
     * 参数类型列表
     */
    val parameterTypes: List<KldType>

    /**
     * 返回类型
     */
    val returnType: KldType

    /**
     * 抛出的异常类型
     */
    val thrownTypes: List<KldType>

    /**
     * 类型变量列表
     */
    val typeVariables: List<KldTypeVariableType>

    /**
     * 接收者类型
     */
    val receiverType: KldType?
}

/**
 * 联合类型
 */
interface KldUnionType : KldType {
    /**
     * 联合的类型列表
     */
    val alternatives: List<KldType>
}

/**
 * 交集类型
 */
interface KldIntersectionType : KldType {
    /**
     * 交集的类型列表
     */
    val bounds: List<KldType>
}

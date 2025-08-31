package com.addzero.kmp.kaleidoscope.core

/**
 * 代码元素抽象接口
 *
 * 统一表示 APT 和 KSP 中的各种代码元素（类、方法、字段等）
 * 提供跨平台一致的代码分析能力
 */
interface KldElement {

    /**
     * 元素名称
     */
    val simpleName: String

    /**
     * 完全限定名称
     */
    val qualifiedName: String?

    /**
     * 元素类型
     */
    val kldElementType: KldElementType

    /**
     * 所属包名
     */
    val packageName: String?

    /**
     * 父级元素
     */
    val enclosingElement: KldElement?

    /**
     * 子级元素列表
     */
    val enclosedElements: List<KldElement>

    /**
     * 注解列表
     */
    val annotations: List<KldAnnotation>

    /**
     * 修饰符列表
     */
    val kldModifiers: Set<KldModifier>

    /**
     * 元素文档注释
     */
    val documentation: String?

    /**
     * 源文件信息
     */
    val kldSourceFile: KldSourceFile?

    /**
     * 获取指定类型的注解（通过全限定名称）
     */
    fun getAnnotation(qualifiedName: String): KldAnnotation?

    /**
     * 检查是否存在指定注解（通过简单名称）
     */
    fun hasAnnotation(simpleName: String): Boolean

    /**
     * 检查是否存在指定注解（通过全限定名称）
     */
    fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean

    /**
     * 获取所有指定类型的注解（通过全限定名称）
     */
    fun getAnnotations(qualifiedName: String): List<KldAnnotation>

    /**
     * 验证此元素是否有效（KSP特有概念的抽象）
     */
    fun isValid(): Boolean
}

/**
 * 元素类型枚举
 */
enum class KldElementType {
    /** 包 */
    PACKAGE,

    /** 类或接口 */
    TYPE,

    /** 类 */
    CLASS,

    /** 接口 */
    INTERFACE,

    /** 枚举 */
    ENUM,

    /** 注解类型 */
    ANNOTATION_TYPE,

    /** 方法 */
    METHOD,

    /** 构造函数 */
    CONSTRUCTOR,

    /** 字段 */
    FIELD,

    /** 属性（Kotlin特有） */
    PROPERTY,

    /** 枚举常量 */
    ENUM_CONSTANT,

    /** 参数 */
    PARAMETER,

    /** 类型参数 */
    TYPE_PARAMETER,

    /** 本地变量 */
    LOCAL_VARIABLE,

    /** 异常参数 */
    EXCEPTION_PARAMETER,

    /** 资源变量 */
    RESOURCE_VARIABLE,

    /** 模块 */
    MODULE,

    /** 其他 */
    OTHER
}

/**
 * 修饰符枚举
 */
enum class KldModifier {
    PUBLIC,
    PRIVATE,
    PROTECTED,
    INTERNAL,
    STATIC,
    FINAL,
    ABSTRACT,
    SEALED,
    OPEN,
    OVERRIDE,
    VIRTUAL,
    SYNCHRONIZED,
    NATIVE,
    STRICTFP,
    VOLATILE,
    TRANSIENT,
    DEFAULT,
    INLINE,
    SUSPEND,
    INFIX,
    OPERATOR,
    DATA,
    INNER,
    COMPANION,
    LATEINIT,
    CONST,
    CROSSINLINE,
    NOINLINE,
    REIFIED,
    EXTERNAL,
    TAILREC,
    VARARG,
    OUT,
    IN
}

/**
 * 源文件信息
 */
interface KldSourceFile {

    /**
     * 文件路径
     */
    val filePath: String

    /**
     * 文件名
     */
    val fileName: String

    /**
     * 包名
     */
    val packageName: String

    /**
     * 文件内容
     */
    val content: String?

    /**
     * 是否为生成的文件
     */
    val isGenerated: Boolean
}

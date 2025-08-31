package com.addzero

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

/**
 * 缓存机制，用于存储编译时获取的元素信息
 */
object KspCache {
    private val classCache = mutableMapOf<String, KSClassDeclaration>()
    private val functionCache = mutableMapOf<String, KSFunctionDeclaration>()
    private val propertyCache = mutableMapOf<String, KSPropertyDeclaration>()
    
    fun getClass(resolver: Resolver, qualifiedName: String): KSClassDeclaration? {
        return classCache.getOrPut(qualifiedName) {
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(qualifiedName)) ?: return null
        }
    }
    
    fun getFunction(declaration: KSClassDeclaration, name: String): KSFunctionDeclaration? {
        val key = "${declaration.qualifiedName?.asString()}.${name}"
        return functionCache.getOrPut(key) {
            declaration.getDeclaredFunctions().find { it.simpleName.asString() == name } ?: return null
        }
    }
    
    fun getProperty(declaration: KSClassDeclaration, name: String): KSPropertyDeclaration? {
        val key = "${declaration.qualifiedName?.asString()}.${name}"
        return propertyCache.getOrPut(key) {
            declaration.getDeclaredProperties().find { it.simpleName.asString() == name } ?: return null
        }
    }
    
    fun clear() {
        classCache.clear()
        functionCache.clear()
        propertyCache.clear()
    }
}

/**
 * 获取父类链
 */
fun getParentClasses(klass: KSClassDeclaration): List<KSClassDeclaration> {
    val parents = mutableListOf<KSClassDeclaration>()
    var current = klass.parentDeclaration
    while (current is KSClassDeclaration) {
        parents.add(current)
        current = current.parentDeclaration
    }
    return parents.reversed()
}

/**
 * 从注解中获取属性值
 */
inline fun <reified T : Any> getAnnoProperty(
    annotation: KSAnnotation,
    propName: String = "value",
    propType: KClass<T>
): T {
    val value = annotation.arguments.first { it.name?.asString() == propName }.value as T
    return value
}

/**
 * 安全地从注解中获取属性值，如果不存在则返回默认值
 */
inline fun <reified T : Any> getAnnoPropertyOrDefault(
    annotation: KSAnnotation,
    propName: String,
    defaultValue: T,
    propType: KClass<T>
): T {
    val arg = annotation.arguments.find { it.name?.asString() == propName } ?: return defaultValue
    return arg.value as? T ?: defaultValue
}

/**
 * 获取类的所有注解
 */
fun KSClassDeclaration.getAllAnnotations(): Sequence<KSAnnotation> {
    return annotations.filter { it.annotationType.resolve().declaration.validate() }
}

/**
 * 检查类是否有指定的注解
 */
fun KSClassDeclaration.hasAnnotation(annotationName: String): Boolean {
    return annotations.any { 
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName ||
        it.shortName.asString() == annotationName.substringAfterLast('.')
    }
}

/**
 * 获取类的特定注解
 */
fun KSClassDeclaration.getAnnotationByName(annotationName: String): KSAnnotation? {
    return annotations.find { 
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName ||
        it.shortName.asString() == annotationName.substringAfterLast('.')
    }
}

/**
 * 获取函数的所有注解
 */
fun KSFunctionDeclaration.getAllAnnotations(): Sequence<KSAnnotation> {
    return annotations.filter { it.annotationType.resolve().declaration.validate() }
}

/**
 * 检查函数是否有指定的注解
 */
fun KSFunctionDeclaration.hasAnnotation(annotationName: String): Boolean {
    return annotations.any { 
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName ||
        it.shortName.asString() == annotationName.substringAfterLast('.')
    }
}

/**
 * 获取函数的特定注解
 */
fun KSFunctionDeclaration.getAnnotationByName(annotationName: String): KSAnnotation? {
    return annotations.find { 
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName ||
        it.shortName.asString() == annotationName.substringAfterLast('.')
    }
}

/**
 * 获取属性的所有注解
 */
fun KSPropertyDeclaration.getAllAnnotations(): Sequence<KSAnnotation> {
    return annotations.filter { it.annotationType.resolve().declaration.validate() }
}

/**
 * 检查属性是否有指定的注解
 */
fun KSPropertyDeclaration.hasAnnotation(annotationName: String): Boolean {
    return annotations.any { 
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName ||
        it.shortName.asString() == annotationName.substringAfterLast('.')
    }
}

/**
 * 获取属性的特定注解
 */
fun KSPropertyDeclaration.getAnnotationByName(annotationName: String): KSAnnotation? {
    return annotations.find { 
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName ||
        it.shortName.asString() == annotationName.substringAfterLast('.')
    }
}

/**
 * 获取文件中的所有类声明
 */
fun KSFile.getAllClassDeclarations(): Sequence<KSClassDeclaration> {
    return declarations.filterIsInstance<KSClassDeclaration>()
}

/**
 * 获取文件中的所有顶级函数声明
 */
fun KSFile.getAllFunctionDeclarations(): Sequence<KSFunctionDeclaration> {
    return declarations.filterIsInstance<KSFunctionDeclaration>()
}

/**
 * 获取文件中的所有顶级属性声明
 */
fun KSFile.getAllPropertyDeclarations(): Sequence<KSPropertyDeclaration> {
    return declarations.filterIsInstance<KSPropertyDeclaration>()
}

/**
 * 获取类的完整限定名
 */
fun KSClassDeclaration.getQualifiedName(): String {
    return qualifiedName?.asString() ?: throw IllegalStateException("Class must have a qualified name")
}

/**
 * 获取类的简单名称
 */
fun KSClassDeclaration.getSimpleName(): String {
    return simpleName.asString()
}

/**
 * 获取类的包名
 */
fun KSClassDeclaration.getPackageName(): String {
    return packageName.asString()
}

/**
 * 获取类的主构造函数
 */
fun KSClassDeclaration.getPrimaryConstructor(): KSFunctionDeclaration? {
    return primaryConstructor
}

/**
 * 获取类的所有构造函数
 */
fun KSClassDeclaration.getAllConstructors(): Sequence<KSFunctionDeclaration> {
    return getDeclaredFunctions().filter { it.functionKind == FunctionKind.MEMBER && it.simpleName.asString() == "<init>" }
}

/**
 * 获取类的所有泛型参数
 */
fun KSClassDeclaration.getAllTypeParameters(): List<KSTypeParameter> {
    return typeParameters.toList()
}

/**
 * 获取函数的返回类型
 */
fun KSFunctionDeclaration.getReturnType(): KSType {
    return returnType?.resolve() ?: throw IllegalStateException("Function must have a return type")
}

/**
 * 获取函数的所有参数
 */
fun KSFunctionDeclaration.getAllParameters(): List<KSValueParameter> {
    return parameters.toList()
}

/**
 * 获取属性的类型
 */
fun KSPropertyDeclaration.getPropertyType(): KSType {
    return type.resolve()
}

/**
 * 获取类型的完整字符串表示，包括泛型参数
 */
fun KSType.getFullTypeName(): String {
    val baseType = declaration.qualifiedName?.asString() ?: "Any"
    val nullableSuffix = if (isMarkedNullable) "?" else ""
    
    // 如果没有泛型参数，直接返回基本类型
    if (arguments.isEmpty()) {
        return "$baseType$nullableSuffix"
    }
    
    // 处理泛型参数
    val genericArgs = arguments.joinToString(", ") { arg ->
        arg.type?.resolve()?.getFullTypeName() ?: "Any"
    }
    
    return "$baseType<$genericArgs>$nullableSuffix"
}

/**
 * 检查类型是否为可空类型
 */
fun KSType.isNullable(): Boolean {
    return isMarkedNullable
}

/**
 * 检查类型是否为基本类型（Int, Long, Boolean等）
 */
fun KSType.isPrimitive(): Boolean {
    val name = declaration.qualifiedName?.asString() ?: return false
    return name == "kotlin.Int" || 
           name == "kotlin.Long" || 
           name == "kotlin.Double" || 
           name == "kotlin.Float" || 
           name == "kotlin.Boolean" || 
           name == "kotlin.Char" || 
           name == "kotlin.Byte" || 
           name == "kotlin.Short"
}

/**
 * 检查类型是否为字符串类型
 */
fun KSType.isString(): Boolean {
    val name = declaration.qualifiedName?.asString() ?: return false
    return name == "kotlin.String"
}

/**
 * 检查类型是否为集合类型
 */
fun KSType.isCollection(): Boolean {
    val name = declaration.qualifiedName?.asString() ?: return false
    return name.startsWith("kotlin.collections.") && 
          (name.contains("List") || name.contains("Set") || name.contains("Map"))
}

/**
 * KSP日志工具类，提供更丰富的日志打印功能
 */
object KspLoggerUtil {
    private const val DEFAULT_TAG = "[KSP-DSL-Builder]"
    private var logger: KSPLogger? = null
    
    /**
     * 初始化日志工具
     * @param kspLogger KSP提供的日志对象
     */
    fun init(kspLogger: KSPLogger) {
        logger = kspLogger
    }
    
    /**
     * 打印信息日志
     * @param message 日志消息
     * @param tag 日志标签，默认为[DEFAULT_TAG]
     */
    fun info(message: String, tag: String = DEFAULT_TAG) {
        logger?.info("$tag $message")
    }
    
    /**
     * 打印警告日志
     * @param message 日志消息
     * @param tag 日志标签，默认为[DEFAULT_TAG]
     */
    fun warn(message: String, tag: String = DEFAULT_TAG) {
        logger?.warn("$tag $message")
    }
    
    /**
     * 打印错误日志
     * @param message 日志消息
     * @param tag 日志标签，默认为[DEFAULT_TAG]
     */
    fun error(message: String, tag: String = DEFAULT_TAG) {
        logger?.error("$tag $message")
    }
    
    /**
     * 打印调试日志（仅在调试模式下输出）
     * @param message 日志消息
     * @param tag 日志标签，默认为[DEFAULT_TAG]
     */
    fun debug(message: String, tag: String = DEFAULT_TAG) {
        logger?.logging("$tag $message")
    }
    
    /**
     * 打印异常日志
     * @param e 异常对象
     * @param message 附加消息，默认为空
     * @param tag 日志标签，默认为[DEFAULT_TAG]
     */
    fun exception(e: Throwable, message: String = "", tag: String = DEFAULT_TAG) {
        val exceptionMessage = if (message.isNotEmpty()) {
            "$message\n${e.stackTraceToString()}"
        } else {
            e.stackTraceToString()
        }
        logger?.error("$tag $exceptionMessage")
    }
}

/**
 * 安全地记录日志（扩展函数）
 */
fun KSPLogger.logInfo(message: String) {
    this.info(message)
}

fun KSPLogger.logWarning(message: String) {
    this.warn(message)
}

fun KSPLogger.logError(message: String) {
    this.error(message)
}

/**
 * 获取类的所有内部类
 */
fun KSClassDeclaration.getAllInnerClasses(): Sequence<KSClassDeclaration> {
    return declarations.filterIsInstance<KSClassDeclaration>()
}

/**
 * 获取类的所有超类型
 */
fun KSClassDeclaration.getAllSuperTypes(): Sequence<KSType> {
    return superTypes.map { it.resolve() }
}

/**
 * 检查类是否实现了指定接口
 */
fun KSClassDeclaration.implementsInterface(interfaceName: String): Boolean {
    return superTypes
        .map { it.resolve() }
        .any { it.declaration.qualifiedName?.asString() == interfaceName }
}

/**
 * 检查类是否继承自指定类
 */
fun KSClassDeclaration.extendsClass(className: String): Boolean {
    return superTypes
        .map { it.resolve() }
        .any { it.declaration.qualifiedName?.asString() == className }
}

///**
// * 获取类的可见性修饰符
// */
//fun KSClassDeclaration.getVisibility(): Visibility {
//    return modifiers.find { it is Modifier.VisibilityModifier }?.let {
//        when (it) {
//            is Modifier.Public -> Visibility.PUBLIC
//            is Modifier.Private -> Visibility.PRIVATE
//            is Modifier.Protected -> Visibility.PROTECTED
//            is Modifier.Internal -> Visibility.INTERNAL
//            else -> Visibility.PUBLIC
//        }
//    } ?: Visibility.PUBLIC
//}

/**
 * 可见性枚举
 */
enum class Visibility {
    PUBLIC, PRIVATE, PROTECTED, INTERNAL
}
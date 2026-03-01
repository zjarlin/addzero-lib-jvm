package site.addzero.lsi.ksp.clazz

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import site.addzero.lsi.clazz.LsiClass

/**
 * 将 [LsiClass] 解包为底层的 [KSClassDeclaration]。
 * 仅当底层实现为 [KspLsiClass] 时有效，否则抛出 [IllegalArgumentException]。
 * 供需要访问 KSP 原生类型系统（如 isAssignableFrom）的调用方使用。
 */
fun LsiClass.toKSClassDeclaration(): KSClassDeclaration =
    (this as? KspLsiClass)?.ksClassDeclaration
        ?: error("LsiClass is not backed by KspLsiClass: ${this::class.qualifiedName}")

/**
 * KSP 环境下 LsiClass → kotlinpoet ClassName 的桥接扩展。
 *
 * 设计说明：
 * - lsi-core 刻意不依赖 kotlinpoet（语言无关），此扩展放在 lsi-ksp 模块，
 *   仅在需要代码生成的 KSP 处理器中引入。
 * - [nameTransformer] 可对 simpleName 做变换，例如生成 "BookDraft"、"BookProps" 等衍生类名。
 */

/**
 * 将 [LsiClass] 转换为 kotlinpoet [ClassName]。
 *
 * @param nameTransformer 对 simpleName 的变换函数，默认不变换。
 */
fun LsiClass.toClassName(nameTransformer: (String) -> String = { it }): ClassName {
    val qn = requireNotNull(qualifiedName) { "LsiClass.qualifiedName must not be null" }
    val pkg = qn.substringBeforeLast('.', missingDelimiterValue = "")
    val simple = requireNotNull(simpleName) { "LsiClass.simpleName must not be null" }
    return ClassName(pkg, nameTransformer(simple))
}

/**
 * 将 [LsiClass] 转换为带嵌套类路径的 kotlinpoet [ClassName]。
 *
 * @param namesTransformer 对名称列表（第一个元素为 simpleName）做整体变换，
 *   返回值将作为 [ClassName] 的 simpleNames 列表。
 */
fun LsiClass.toNestedClassName(
    namesTransformer: (String) -> List<String> = { listOf(it) }
): ClassName {
    val qn = requireNotNull(qualifiedName) { "LsiClass.qualifiedName must not be null" }
    val pkg = qn.substringBeforeLast('.', missingDelimiterValue = "")
    val simple = requireNotNull(simpleName) { "LsiClass.simpleName must not be null" }
    val names = namesTransformer(simple)
    require(names.isNotEmpty()) { "namesTransformer must return at least one name" }
    return ClassName(pkg, names.first(), *names.drop(1).toTypedArray())
}

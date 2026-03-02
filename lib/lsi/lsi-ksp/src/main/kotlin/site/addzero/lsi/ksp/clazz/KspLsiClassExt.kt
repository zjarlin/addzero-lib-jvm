package site.addzero.lsi.ksp.clazz

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import site.addzero.lsi.clazz.LsiClass

fun KSClassDeclaration?.isEnum(): Boolean {
  return this?.classKind == ClassKind.ENUM_CLASS
}
/**
 * KSP符号到LSI对象的转换扩展函数
 * 这些扩展函数提供了便利的API来将KSP符号转换为LSI接口实现
 */

/**
 * 将KSClassDeclaration转换为LsiClass
 */
fun KSClassDeclaration.toLsiClass(resolver: Resolver): LsiClass =
  KspLsiClass(resolver, this)

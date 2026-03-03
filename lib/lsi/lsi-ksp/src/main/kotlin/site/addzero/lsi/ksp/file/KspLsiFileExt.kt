package site.addzero.lsi.ksp.file

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

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


package site.addzero.lsi.ksp.field

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import site.addzero.lsi.assist.TypeChecker.toSimpleName
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.ksp.clazz.isEnum
import site.addzero.lsi.ksp.type.getFullQualifiedTypeString

@Deprecated("use LsiField.typeName")
fun KSPropertyDeclaration.typeName(): String {
  val ktType = this.type.resolve().declaration.simpleName.asString()
  return ktType
}

/**
 * 将KSPropertyDeclaration转换为LsiField
 */
fun KSPropertyDeclaration.toLsiField(resolver: Resolver): LsiField = KspLsiField(resolver, this)

/**
 * 此方法不健全,不建议用
 */
fun KSPropertyDeclaration.isT(): Boolean {
  val type = this.type.resolve()
  val declaration = type.declaration
  // 情况1：声明是类（且不是基本类型）
  if (declaration is KSClassDeclaration) {
    val qualifiedName = declaration.qualifiedName?.asString()

    // 排除Kotlin/Java的基本类型
    return qualifiedName !in setOf(
      "kotlin.String",
      "kotlin.Int",
      "kotlin.Long",
      "kotlin.Boolean",
      "kotlin.Float",
      "kotlin.Double",
      "kotlin.Byte",
      "kotlin.Short",
      "kotlin.Char",
      "java.lang.String",
      "java.lang.Integer"
    )
  }
  return true
}

fun KSPropertyDeclaration.isEnum(): Boolean {
  return this.type.resolve().declaration.let { decl ->
    (decl as? KSClassDeclaration)?.isEnum() ?: false
  }
}

/**
 * 获取属性的所有注解
 */
fun KSPropertyDeclaration.getAllAnnotations(): Sequence<KSAnnotation> {
  return annotations.filter { it.annotationType.resolve().declaration.validate() }
}

/**
 * 获取属性的特定注解
 */
fun KSPropertyDeclaration.getAnnotationByName(qualifiedName: String): KSAnnotation? {
  return annotations.find {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName ||
      it.shortName.asString() == qualifiedName.toSimpleName()
  }
}

/**
 * 检查属性是否有指定的注解
 */
fun KSPropertyDeclaration.hasAnnotation(qualifiedName: String): Boolean {
  return annotations.any {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName ||
      it.shortName.asString() == qualifiedName.toSimpleName()
  }
}

/**
 * 获取属性的全限定类型字符串（包含泛型参数和可空性）
 * 简化版本，只进行基础类型解析
 */
fun KSPropertyDeclaration.getQualifiedTypeString(): String {
  return try {
    val type = this.type.resolve()
    type.getFullQualifiedTypeString()
  } catch (e: Exception) {
    // 如果类型解析失败，尝试使用原始类型字符串
    val rawTypeString = this.type.toString()
    if (rawTypeString.contains("<ERROR") ||
      rawTypeString.any { !it.isLetterOrDigit() && it != '.' && it != '_' && it != '$' && it != '<' && it != '>' && it != '?' && it != ',' && it != ' ' }
    ) {
      "kotlin.Any"
    } else {
      rawTypeString
    }
  }
}

val KSPropertyDeclaration.resolveType get() = this.type.resolve()
val KSPropertyDeclaration.isRequired get() = !this.resolveType.isMarkedNullable
val KSPropertyDeclaration.typeDecl get() = resolveType.declaration

val KSPropertyDeclaration.generics: List<KSClassDeclaration?>
  get() {
    // 正确的方式：通过 resolve() 获取类型参数
    val resolvedType = this.type.resolve()
    val firstTypeArgument = resolvedType.arguments.map {
      val resolve = it.type?.resolve()
      val clazz = resolve?.declaration as? KSClassDeclaration
      clazz
    }
    return firstTypeArgument
  }

/**
 * 获取属性的简化类型字符串（不包含包名，但保留泛型）
 */
fun KSPropertyDeclaration.getSimpleTypeString(): String {
  val type = this.type.resolve()
  return buildString {
    // 基础类型名称
    append(type.declaration.simpleName.asString())

    // 处理泛型参数
    if (type.arguments.isNotEmpty()) {
      append("<")
      append(type.arguments.joinToString(", ") { arg ->
        arg.type?.resolve()?.let {
          it.declaration.simpleName.asString()
        } ?: "*"
      })
      append(">")
    }

    // 处理可空性
    if (type.nullability == Nullability.NULLABLE) {
      append("?")
    }
  }
}



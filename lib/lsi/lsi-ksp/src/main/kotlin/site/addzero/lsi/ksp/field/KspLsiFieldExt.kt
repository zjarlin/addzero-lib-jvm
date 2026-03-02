package site.addzero.lsi.ksp.field

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.ksp.clazz.isEnum

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

fun LsiField.defaultValue(): String {
  if (this.name == "id") {
    return "null"
  }
  val type = this.type
  val typeDecl = type?.lsiClass
  val fullTypeName = typeDecl?.qualifiedName
  val typeName = typeDecl?.simpleName
  val isNullable = type.isNullable
  return when {

    typeDecl.isEnum() -> {
      if (isNullable) "null" else "${fullTypeName}.entries.first()"
    }

    isNullable -> "null"
    typeName == "String" -> "\"\""
    typeName == "Int" -> "0"
    typeName == "Long" -> "0L"
    typeName == "Double" -> "0.0"
    typeName == "Float" -> "0f"
    typeName == "Boolean" -> "false"
    typeName == "List" -> "emptyList()"
    typeName == "Set" -> "emptySet()"
    typeName == "Map" -> "emptyMap()"
    typeName == "LocalDateTime" -> "kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())"
    typeName == "LocalDate" -> "kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date"
    typeName == "LocalTime" -> "kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).time"
    else -> ""
  }
}

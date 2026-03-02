package site.addzero.util

import com.google.devtools.ksp.symbol.*
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.str.removeAnyQuote
import site.addzero.util.str.toUnderLineCase
import kotlin.text.isNullOrEmpty

val KSPropertyDeclaration.firstTypeArgumentKSClassDeclaration: KSClassDeclaration?
  get() {
    return try {
      // 正确的方式：通过 resolve() 获取类型参数
      val resolvedType = this.type.resolve()
      val firstTypeArgument = resolvedType.arguments.firstOrNull()
      val firstType: KSType? = firstTypeArgument?.type?.resolve()
      val firstClassDeclaration: KSClassDeclaration? = firstType?.declaration as? KSClassDeclaration
      firstClassDeclaration
    } catch (e: Exception) {
      println("获取泛型类型失败: ${this.simpleName.asString()}, 错误: ${e.message}")
      null
    }
  }

val KSPropertyDeclaration.name: String
  get() = this.simpleName.asString()
val KSPropertyDeclaration.resolveType: KSType
  get() = this.type.resolve()
val KSPropertyDeclaration.isRequired: Boolean
  get() = !this.resolveType.isMarkedNullable

val KSPropertyDeclaration.typeDecl: KSDeclaration  // 属性类型的声明
  get() = resolveType.declaration

val KSPropertyDeclaration.typeName: String  // 属性类型的简单名称
  get() = this.typeDecl.simpleName.asString()

// 生成默认值
val KSPropertyDeclaration.defaultValue: String
  get() = this.defaultValue()
// 判断是否需要导入对于form来说,递归可能这里会生成无用的iso

val KSPropertyDeclaration.label: String
  get() {
    // 优先从@Label注解获取值
    val anno = this.getAnno("Label")
    if (anno != null) {
      val argFirstValue = anno.getArgFirstValue()
      return argFirstValue?.makeSurroundWith("\"") ?: ""
    }

    // 没有@Label注解则使用原来的逻辑
    return (this.docString ?: name).removeAnyQuote().makeSurroundWith("\"")
  }



fun KSPropertyDeclaration.isCollectionType(): Boolean {
  val type = this.type.resolve()
  val declaration = type.declaration

  // 获取类型的全限定名（如 "kotlin.collections.List"）
  val typeName = declaration.qualifiedName?.asString() ?: return false

  // 检查是否是常见集合类型
  return typeName in setOf(
    "kotlin.collections.List",
    "kotlin.collections.MutableList",
    "kotlin.collections.Set",
    "kotlin.collections.MutableSet",
    "kotlin.collections.Map",
    "kotlin.collections.MutableMap",
    "java.util.List",
    "java.util.ArrayList",
    "java.util.Set",
    "java.util.HashSet",
    "java.util.Map",
    "java.util.HashMap"
  )
}

/**
 * 猜测Jimmer实体的表名
 * 1. 优先读取@Table注解的name属性
 * 2. 没有则尝试从KDoc注释中提取@table标签
 * 3. 没有则用类名转下划线
 */
fun guessTableName(ktClass: KSClassDeclaration): String {
  // 1. 优先读取@Table注解
  val tableAnn = ktClass.annotations.firstOrNull {
    it.shortName.asString() == "Table"
  }
  val tableNameFromAnn = tableAnn?.arguments?.firstOrNull { it.name?.asString() == "name" }?.value as? String ?: ""
  if (!tableNameFromAnn.isNullOrEmpty()) {
    return tableNameFromAnn
  }

  // 2. 尝试从KDoc注释中提取@table标签
  val doc = ktClass.docString ?: ""
  if (!doc.isNullOrEmpty()) {
    // 支持 @table 表名 或 @table:表名
    val regex = Regex("@table[:：]?\\s*([\\w_]+)")
    val match = regex.find(doc)
    if (match != null) {
      return match.groupValues[1]
    }
  }

  // 3. 默认用类名转下划线
  val asString = ktClass.simpleName.asString().toUnderLineCase()
  return asString
}

fun KSPropertyDeclaration.hasAnno(string: String): Boolean {
  return this.getAnno(string) != null
}

/**
 * 获取完整的类型字符串表达，包括泛型、注解、函数类型等
 * 用于 @ComposeAssist 等需要精确类型信息的场景
 */
fun KSType.getCompleteTypeString(): String {
  return buildString {
    // 获取基础类型名称
    val declaration = this@getCompleteTypeString.declaration
    val baseTypeName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()

    // 处理函数类型
    if (baseTypeName.startsWith("kotlin.Function")) {
      // 对于函数类型，先处理注解
      val annotations = this@getCompleteTypeString.annotations.toList()
      if (annotations.isNotEmpty()) {
        val annotationStrings = annotations.map { annotation ->
          val shortName = annotation.shortName.asString()
          val args = annotation.arguments
          if (args.isNotEmpty()) {
            val argString = args.joinToString(", ") { arg ->
              "${arg.name?.asString() ?: ""}=${arg.value}"
            }
            "[$shortName($argString)]"
          } else {
            "[$shortName]"
          }
        }
        append(annotationStrings.joinToString(" "))
        append(" ")
      }

      // 构建函数类型字符串
      val functionTypeString = buildFunctionTypeString()

      // 处理可空性 - 对于函数类型，可空性标记应该包裹整个函数类型
      if (this@getCompleteTypeString.isMarkedNullable) {
        append("(")
        append(functionTypeString)
        append(")?")
      } else {
        append(functionTypeString)
      }
    } else {
      // 对于非函数类型，处理注解
      val annotations = this@getCompleteTypeString.annotations.toList()
      if (annotations.isNotEmpty()) {
        val annotationStrings = annotations.map { annotation ->
          val shortName = annotation.shortName.asString()
          val args = annotation.arguments
          if (args.isNotEmpty()) {
            val argString = args.joinToString(", ") { arg ->
              "${arg.name?.asString() ?: ""}=${arg.value}"
            }
            "[$shortName($argString)]"
          } else {
            "[$shortName]"
          }
        }
        append(annotationStrings.joinToString(" "))
        append(" ")
      }

      append(baseTypeName)

      // 处理泛型参数
      val typeArguments = this@getCompleteTypeString.arguments
      if (typeArguments.isNotEmpty()) {
        append("<")
        append(typeArguments.joinToString(", ") { arg ->
          when (arg.variance) {
            Variance.STAR -> "*"
            Variance.CONTRAVARIANT -> "in ${arg.type?.resolve()?.getCompleteTypeString() ?: "*"}"
            Variance.COVARIANT -> "out ${arg.type?.resolve()?.getCompleteTypeString() ?: "*"}"
            else -> arg.type?.resolve()?.getCompleteTypeString() ?: "*"
          }
        })
        append(">")
      }

      // 处理可空性
      if (this@getCompleteTypeString.isMarkedNullable) {
        append("?")
      }
    }
  }
}

/**
 * 构建函数类型字符串，如 (T) -> R, @Composable (T) -> Unit 等
 */
private fun KSType.buildFunctionTypeString(): String {
  val declaration = this.declaration
  val baseTypeName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()

  // 解析函数类型的参数数量
  val functionNumber = when {
    baseTypeName == "kotlin.Function0" -> 0
    baseTypeName.startsWith("kotlin.Function") -> {
      baseTypeName.removePrefix("kotlin.Function").toIntOrNull() ?: 0
    }

    else -> 0
  }

  val typeArguments = this.arguments

  return buildString {
    // 函数参数类型
    if (functionNumber > 0 && typeArguments.size > functionNumber) {
      append("(")
      val paramTypes = typeArguments.take(functionNumber).map { arg ->
        arg.type?.resolve()?.getCompleteTypeString() ?: "*"
      }
      append(paramTypes.joinToString(", "))
      append(")")
    } else if (functionNumber == 0) {
      append("()")
    }

    append(" -> ")

    // 返回类型
    val returnType = typeArguments.lastOrNull()?.type?.resolve()?.getCompleteTypeString() ?: "Unit"
    append(returnType)
  }
}

/**
 * 获取参数的完整类型字符串，包括参数注解
 */
fun KSValueParameter.getCompleteTypeString(): String {
  return buildString {
    // 处理参数注解
    val annotations = this@getCompleteTypeString.annotations.toList()
    if (annotations.isNotEmpty()) {
      val annotationStrings = annotations.map { annotation ->
        val shortName = annotation.shortName.asString()
        val args = annotation.arguments
        if (args.isNotEmpty()) {
          val argString = args.joinToString(", ") { arg ->
            val name = arg.name?.asString()
            val value = arg.value
            if (name != null) "$name=$value" else value.toString()
          }
          "@$shortName($argString)"
        } else {
          "@$shortName"
        }
      }
      append(annotationStrings.joinToString(" "))
      append(" ")
    }

    // 获取类型字符串
    append(this@getCompleteTypeString.type.resolve().getCompleteTypeString())
  }
}

/**
 * 获取简化的类型字符串，移除包名但保留泛型和注解
 */
fun KSType.getSimplifiedTypeString(): String {
  return this.getCompleteTypeString()
    .replace("kotlin.collections.", "")
    .replace("kotlin.", "")
    .replace("androidx.compose.runtime.", "")
    .replace("androidx.compose.ui.", "")
    .replace("androidx.compose.foundation.", "")
    .replace("androidx.compose.material3.", "")
}

/**
 * 获取函数的完整签名字符串，包括泛型参数、参数注解等
 */
fun KSFunctionDeclaration.getCompleteSignature(): String {
  return buildString {
    // 函数注解
    val annotations = this@getCompleteSignature.annotations.toList()
    if (annotations.isNotEmpty()) {
      annotations.forEach { annotation ->
        append("@${annotation.shortName.asString()}")
        if (annotation.arguments.isNotEmpty()) {
          append("(")
          append(annotation.arguments.joinToString(", ") { arg ->
            "${arg.name?.asString() ?: ""}=${arg.value}"
          })
          append(")")
        }
        append("\n")
      }
    }

    append("fun ")

    // 泛型参数
    val typeParameters = this@getCompleteSignature.typeParameters
    if (typeParameters.isNotEmpty()) {
      append("<")
      append(typeParameters.joinToString(", ") { typeParam ->
        val name = typeParam.name.asString()
        val bounds = typeParam.bounds.toList()
        if (bounds.isNotEmpty()) {
          val boundsString = bounds.joinToString(" & ") { bound ->
            bound.resolve().getCompleteTypeString()
          }
          "$name : $boundsString"
        } else {
          name
        }
      })
      append("> ")
    }

    append(this@getCompleteSignature.simpleName.asString())
    append("(")

    // 参数列表
    val parameters = this@getCompleteSignature.parameters
    append(parameters.joinToString(",\n    ") { param ->
      val paramName = param.name?.asString() ?: ""
      val paramType = param.getCompleteTypeString()
      val defaultValue = if (param.hasDefault) " = ..." else ""
      "$paramName: $paramType$defaultValue"
    })

    append(")")

    // 返回类型
    val returnType = this@getCompleteSignature.returnType?.resolve()
    if (returnType != null && returnType.declaration.simpleName.asString() != "Unit") {
      append(": ${returnType.getCompleteTypeString()}")
    }
  }
}

/**
 * 布尔值加法操作符，用于权重计算
 * 将布尔值视为 0 和 1 进行加法运算
 */
operator fun Boolean.plus(other: Boolean): Int = this.toInt() + other.toInt()

/**
 * 布尔值转整数
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * 整数与布尔值加法操作符
 */
operator fun Int.plus(boolean: Boolean): Int = this + boolean.toInt()

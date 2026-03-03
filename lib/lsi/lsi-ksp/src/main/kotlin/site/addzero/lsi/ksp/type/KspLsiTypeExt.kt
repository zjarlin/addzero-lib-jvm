package site.addzero.lsi.ksp.type

import com.google.devtools.ksp.symbol.*

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
 * 检查类型是否为集合类型
 */
fun KSType.isCollection(): Boolean {
  val name = declaration.qualifiedName?.asString() ?: return false
  return name.startsWith("kotlin.collections.") &&
    (name.contains("List") || name.contains("Set") || name.contains("Map"))
}

/**
 * 检查类型是否为字符串类型
 */
fun KSType.isString(): Boolean {
  val name = declaration.qualifiedName?.asString() ?: return false
  return name == "kotlin.String"
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
 * 获取简化的类型字符串，移除包名但保留泛型和注解
 */
fun KSType.getSimplifiedTypeString(): String {
  return this.getCompleteTypeString().replace("kotlin.collections.", "").replace("kotlin.", "").replace("androidx.compose.runtime.", "")
    .replace("androidx.compose.ui.", "").replace("androidx.compose.foundation.", "").replace("androidx.compose.material3.", "")
}

/**
 * 获取 KSType 的完整类型字符串,带泛型（简化版本）
 * 只处理基本的类型解析，不进行复杂的类型映射
 */
fun KSType.getFullQualifiedTypeString(): String {
  return try {
    val type = this
    val qualifiedName = type.declaration.qualifiedName?.asString()
    val simpleName = type.declaration.simpleName.asString()

    // 如果类型名包含错误标记，返回简单类型名
    if (simpleName.contains("<ERROR")) {
      return simpleName.replace("<ERROR", "").replace(">", "")
    }

    // 基础类型名称
    val baseType = qualifiedName ?: simpleName

    // 处理泛型参数
    val genericArgs = if (type.arguments.isNotEmpty()) {
      type.arguments.joinToString(", ") { arg ->
        arg.type?.resolve()?.getFullQualifiedTypeString() ?: "*"
      }
    } else null

    // 处理可空性
    val nullableSuffix = if (type.nullability == Nullability.NULLABLE) "?" else ""

    when {
      genericArgs != null -> "$baseType<$genericArgs>$nullableSuffix"
      else -> "$baseType$nullableSuffix"
    }
  } catch (e: Exception) {
    // 异常时返回简单类型名
    this.declaration.simpleName.asString()
  }
}

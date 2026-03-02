package site.addzero.lsi.ksp.method

import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import site.addzero.lsi.ksp.type.getCompleteTypeString

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
 * 获取函数的所有注解
 */
fun KSFunctionDeclaration.getAnnotations(): Sequence<KSAnnotation> {
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



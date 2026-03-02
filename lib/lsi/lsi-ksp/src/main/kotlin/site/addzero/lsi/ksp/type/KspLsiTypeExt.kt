package site.addzero.lsi.ksp.type

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability








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

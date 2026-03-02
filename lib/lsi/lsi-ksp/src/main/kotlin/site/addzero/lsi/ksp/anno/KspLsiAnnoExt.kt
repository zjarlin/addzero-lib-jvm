package site.addzero.lsi.ksp.anno

import com.google.devtools.ksp.symbol.KSAnnotation

/**
 * 从注解中获取属性值
 */
inline fun <reified T> KSAnnotation.getAttributeWithType(
  attr: String = "value",
): T? {
  val first = arguments.first { it.name?.asString() == attr }
  val value1 = first.value
  val value = value1 as? T
  return value
}

/**
 * 从注解中获取属性值
 */
inline fun <reified T> KSAnnotation.getAttributeWithTypeAndDefaultValue(
  attr: String = "value",
  def: T,
): T {
  return getAttributeWithType<T>() ?: def
}

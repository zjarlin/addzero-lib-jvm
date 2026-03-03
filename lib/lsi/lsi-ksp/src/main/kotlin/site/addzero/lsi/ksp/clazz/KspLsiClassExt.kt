package site.addzero.lsi.ksp.clazz

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import site.addzero.lsi.assist.TypeChecker.toSimpleName
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

/**
 * 获取父类链
 */
fun KSClassDeclaration.getParentClasses(): List<KSClassDeclaration> {
  val parents = mutableListOf<KSClassDeclaration>()
  var current = parentDeclaration
  while (current is KSClassDeclaration) {
    parents.add(current)
    current = current.parentDeclaration
  }
  return parents.reversed()
}

/**
 * 检查类是否有指定的注解
 */
fun KSClassDeclaration.hasAnnotation(qualifiedName: String): Boolean {
  return annotations.any {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName ||
      it.shortName.asString() == qualifiedName.toSimpleName()
  }
}

/**
 * 获取类的特定注解
 */
fun KSClassDeclaration.getAnnotationByName(qualifiedName: String): KSAnnotation? {
  return annotations.find {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName ||
      it.shortName.asString() == qualifiedName.toSimpleName()
  }
}

/**
 * 获取类的包名
 */
fun KSClassDeclaration.getPackageName(): String {
  return packageName.asString()
}

fun KSClassDeclaration.getFunction(simpleName: String): KSFunctionDeclaration? {
  val declaration = getDeclaredFunctions().find { it.simpleName.asString() == simpleName } ?: return null
  return declaration
}

/**
 * 获取类的主构造函数
 */
fun KSClassDeclaration.getPrimaryConstructor(): KSFunctionDeclaration? {
  return primaryConstructor
}

/**
 * 获取类的所有构造函数
 */
fun KSClassDeclaration.getAllConstructors(): Sequence<KSFunctionDeclaration> {
  return getDeclaredFunctions().filter { it.functionKind == FunctionKind.MEMBER && it.simpleName.asString() == "<init>" }
}

/**
 * 获取类的所有泛型参数
 */
fun KSClassDeclaration.getAllTypeParameters(): List<KSTypeParameter> {
  return typeParameters.toList()
}


/**
 * 获取类的所有内部类
 */
fun KSClassDeclaration.getAllInnerClasses(): Sequence<KSClassDeclaration> {
  return declarations.filterIsInstance<KSClassDeclaration>()
}

/**
 * 获取类的所有超类型
 */
fun KSClassDeclaration.getAllSuperTypes(): Sequence<KSType> {
  return superTypes.map { it.resolve() }
}

/**
 * 检查类是否实现了指定接口
 */
fun KSClassDeclaration.implementsInterface(qualifiedName: String): Boolean {
  return superTypes
    .map { it.resolve() }
    .any { it.declaration.qualifiedName?.asString() == qualifiedName }
}

/**
 * 检查类是否继承自指定类
 */
fun KSClassDeclaration.extendsClass(qualifiedName: String): Boolean {
  return superTypes
    .map { it.resolve() }
    .any { it.declaration.qualifiedName?.asString() == qualifiedName }
}

/**
 * 获取类的可见性修饰符
 * @return Visibility枚举值
 */
fun KSClassDeclaration.getLsiVisibility(): Visibility {
  return when {
    Modifier.PRIVATE in modifiers -> Visibility.PRIVATE
    Modifier.PROTECTED in modifiers -> Visibility.PROTECTED
    Modifier.INTERNAL in modifiers -> Visibility.INTERNAL
    Modifier.PUBLIC in modifiers || modifiers.none { it.name.contains("VISIBILITY") } -> Visibility.PUBLIC
    else -> Visibility.PUBLIC
  }
}

/**
 * 可见性枚举
 */
enum class Visibility {
  PUBLIC, PRIVATE, PROTECTED, INTERNAL
}

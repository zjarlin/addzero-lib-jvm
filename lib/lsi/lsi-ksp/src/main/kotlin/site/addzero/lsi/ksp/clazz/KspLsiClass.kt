package site.addzero.lsi.ksp.clazz

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.assist.checkIsPojo
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.ksp.anno.KspLsiAnnotation
import site.addzero.lsi.ksp.field.KspLsiField
import site.addzero.lsi.ksp.method.KspLsiMethod
import site.addzero.lsi.method.LsiMethod

class KspLsiClass(
  internal val resolver: Resolver,
  internal val ksClassDeclaration: KSClassDeclaration,
) : LsiClass {

  override val simpleName by lazy {
    try {
      ksClassDeclaration.simpleName.asString()
    } catch (e: Exception) {
      null
    }
  }
  override val qualifiedName by lazy {
    try {
      ksClassDeclaration.qualifiedName?.asString()
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  override val comment by lazy {
    ksClassDeclaration.docString
  }

  override val fields: List<LsiField> by lazy {
    try {
      ksClassDeclaration.getAllProperties()
        .map { KspLsiField(resolver, it) }
        .toList()
    } catch (e: Exception) {
      emptyList()
    }
  }

  override val annotations: List<LsiAnnotation> by lazy {
    try {
//          ksClassDeclaration.annotations
      ksClassDeclaration.annotations.filter { it.annotationType.resolve().declaration.validate() }
        .map { KspLsiAnnotation(it) }
        .toList()
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  override val isInterface by lazy {
    ksClassDeclaration.classKind == ClassKind.INTERFACE
  }

  override val isEnum by lazy {
    ksClassDeclaration.classKind == ClassKind.ENUM_CLASS
  }

  override val isCollectionType by lazy {
    val name = qualifiedName ?: ""
    name.startsWith("kotlin.collections.") || name.startsWith("java.util.") &&
      (name.contains("List") || name.contains("Set") || name.contains("Collection") || name.contains("Map"))
  }

  override val isPojo by lazy {
    val isDataClass = ksClassDeclaration.modifiers.contains(Modifier.DATA)
    checkIsPojo(
      isInterface = isInterface,
      isEnum = isEnum,
      isAbstract = ksClassDeclaration.modifiers.contains(Modifier.ABSTRACT),
      isDataClass = isDataClass,
      annotationNames = annotations.mapNotNull { it.qualifiedName },
      isShortName = false
    )
  }

  override val superClasses: List<LsiClass> by lazy {
    ksClassDeclaration.superTypes
      .mapNotNull { superType ->
        val resolvedType = superType.resolve()
        val declaration = resolvedType.declaration
        if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.CLASS) {
          KspLsiClass(resolver, declaration)
        } else null
      }
      .toList()
  }

  override val interfaces: List<LsiClass> by lazy {
    ksClassDeclaration.superTypes
      .mapNotNull { superType ->
        val resolvedType = superType.resolve()
        val declaration = resolvedType.declaration
        if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.INTERFACE) {
          KspLsiClass(resolver, declaration)
        } else null
      }
      .toList()
  }

  override val methods: List<LsiMethod> by lazy {
    ksClassDeclaration.getAllFunctions()
      .map { KspLsiMethod(resolver, it) }
      .toList()
  }

  override val fileName by lazy {
    ksClassDeclaration.containingFile?.fileName?.removeSuffix(".kt")
  }

  override val isObject by lazy {
    ksClassDeclaration.classKind == ClassKind.OBJECT
  }

  override val isCompanionObject by lazy {
    ksClassDeclaration.isCompanionObject
  }
}


package site.addzero.lsi.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.ksp.anno.KspLsiAnnotation
import site.addzero.lsi.ksp.clazz.KspLsiClass
import site.addzero.lsi.ksp.field.KspLsiField
import site.addzero.lsi.ksp.method.KspLsiMethod
import site.addzero.lsi.ksp.type.KspLsiType
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.type.LsiType




/**
 * 将KSFunctionDeclaration转换为LsiMethod
 */
fun KSFunctionDeclaration.toLsiMethod(resolver: Resolver): LsiMethod =
  KspLsiMethod(resolver, this)

/**
 * 将KSType转换为LsiType
 */
fun KSType.toLsiType(resolver: Resolver): LsiType =
  KspLsiType(resolver, this)

/**
 * 将KSAnnotation转换为LsiAnnotation
 */
fun KSAnnotation.toLsiAnnotation(resolver: Resolver): LsiAnnotation =
  KspLsiAnnotation(this)

/**
 * 批量转换KSClassDeclaration列表为LsiClass列表
 */
fun List<KSClassDeclaration>.toLsiClasses(resolver: Resolver): List<LsiClass> =
  map { it.toLsiClass(resolver) }

/**
 * 批量转换KSPropertyDeclaration列表为LsiField列表
 */
fun List<KSPropertyDeclaration>.toLsiFields(resolver: Resolver): List<LsiField> =
  map { it.toLsiField(resolver) }

/**
 * 批量转换KSFunctionDeclaration列表为LsiMethod列表
 */
fun List<KSFunctionDeclaration>.toLsiMethods(resolver: Resolver): List<LsiMethod> =
  map { it.toLsiMethod(resolver) }

/**
 * 批量转换KSType列表为LsiType列表
 */
fun List<KSType>.toLsiTypes(resolver: Resolver): List<LsiType> =
  map { it.toLsiType(resolver) }

/**
 * 批量转换KSAnnotation列表为LsiAnnotation列表
 */
fun List<KSAnnotation>.toLsiAnnotations(resolver: Resolver): List<LsiAnnotation> =
  map { it.toLsiAnnotation(resolver) }

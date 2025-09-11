package com.addzero.kld

import com.addzero.kld.processing.KLBuiltIns
import com.addzero.kld.processing.KLResolver
import com.addzero.kld.symbol.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment


fun RoundEnvironment.toKldResolver(processingEnv: ProcessingEnvironment): KLResolver {
    val roundEnv = this
    val elementUtils = processingEnv.elementUtils
    val typeUtils = processingEnv.typeUtils

    return object : KLResolver {
        override fun getNewFiles(): Sequence<KLFile> {
            TODO()
        }

        override fun getAllFiles(): Sequence<KLFile> {
            // APT不直接提供文件，抛出不支持操作异常
            throw UnsupportedOperationException("APT平台不支持getAllFiles()方法")
        }

        override fun getSymbolsWithAnnotation(
            annotationName: String,
            inDepth: Boolean
        ): Sequence<KLAnnotated> {
            val typeElement = elementUtils.getTypeElement(annotationName)
            val asSequence = roundEnv.getElementsAnnotatedWith(typeElement).asSequence()
            // TODO: 完成elemet到KLAnnotated

            val map = asSequence.map { element -> element as KLAnnotated } // 简化映射

            return map
        }

        override fun getClassDeclarationByName(name: KLName): KLClassDeclaration? {
            val typeElement = elementUtils.getTypeElement(name.asString())
            //todo 完成 typeElement到KLClassDeclaration的转换
            return typeElement as KLClassDeclaration?
        }

        override fun getFunctionDeclarationsByName(
            name: KLName,
            includeTopLevel: Boolean
        ): Sequence<KLFunctionDeclaration> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getPropertyDeclarationByName(
            name: KLName,
            includeTopLevel: Boolean
        ): KLPropertyDeclaration? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getTypeArgument(
            typeRef: KLTypeReference,
            variance: Variance
        ): KLTypeArgument {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getKLNameFromString(name: String): KLName {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun createKLTypeReferenceFromKLType(type: KLType): KLTypeReference {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override val builtIns: KLBuiltIns
            get() = TODO("Not yet implemented")

        override fun mapToJvmSignature(declaration: KLDeclaration): String? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun overrides(
            overrider: KLDeclaration,
            overridee: KLDeclaration
        ): Boolean {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun overrides(
            overrider: KLDeclaration,
            overridee: KLDeclaration,
            containingClass: KLClassDeclaration
        ): Boolean {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getJvmName(declaration: KLFunctionDeclaration): String? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getJvmName(accessor: KLPropertyAccessor): String? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getOwnerJvmClassName(declaration: KLPropertyDeclaration): String? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getOwnerJvmClassName(declaration: KLFunctionDeclaration): String? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getJvmCheckedException(function: KLFunctionDeclaration): Sequence<KLType> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getJvmCheckedException(accessor: KLPropertyAccessor): Sequence<KLType> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getDeclarationsFromPackage(packageName: String): Sequence<KLDeclaration> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun mapJavaNameToKotlin(javaName: KLName): KLName? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun mapKotlinNameToJava(kotlinName: KLName): KLName? {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getDeclarationsInSourceOrder(container: KLDeclarationContainer): Sequence<KLDeclaration> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun effectiveJavaModifiers(declaration: KLDeclaration): Set<Modifier> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getJavaWildcard(reference: KLTypeReference): KLTypeReference {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun isJavaRawType(type: KLType): Boolean {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getPackageAnnotations(packageName: String): Sequence<KLAnnotation> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getPackagesWithAnnotation(annotationName: String): Sequence<String> {
            // TODO: 待实现
            TODO("Not yet implemented")
        }

        override fun getModuleName(): KLName {
            // TODO: 待实现
            TODO("Not yet implemented")
        }
    }

}

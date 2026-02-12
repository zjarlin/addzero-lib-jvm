package site.addzero.ioc

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import site.addzero.ioc.annotation.Bean
import site.addzero.ioc.container.ContainerGenerator
import site.addzero.ioc.strategy.BeanInfo
import site.addzero.ioc.strategy.InitType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.hasNoArgConstructor
import site.addzero.util.lsi.method.hasNoRequiredParameters
import site.addzero.util.lsi.method.isComposable
import site.addzero.util.lsi.method.isSuspend
import site.addzero.util.lsi.method.parentClass
import site.addzero.util.lsi_impl.impl.ksp.toLsiClass
import site.addzero.util.lsi_impl.impl.ksp.toLsiMethod

class IocProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = object : SymbolProcessor {
        private val beans = mutableListOf<BeanInfo>()
        private val classComponents = mutableListOf<LsiClass>()

        /**
         * KSP options:
         *   ioc.module  — generated package name (auto-derived from first bean if absent)
         *   ioc.role    — "app" generates Ioc object + SPI; "lib" (default) generates SPI only
         */
        private val modulePackage: String? = environment.options["ioc.module"]
        private val isApp: Boolean = environment.options["ioc.role"] == "app"

        private fun extractBeanAnnotation(annotated: KSAnnotated): Triple<String, Int, List<String>> {
            val beanAnnotation = annotated.annotations.firstOrNull {
                it.shortName.asString() == "Bean"
            }
            val name = beanAnnotation?.arguments?.firstOrNull {
                it.name?.asString() == "name"
            }?.value as? String ?: ""
            val order = beanAnnotation?.arguments?.firstOrNull {
                it.name?.asString() == "order"
            }?.value as? Int ?: 0
            @Suppress("UNCHECKED_CAST")
            val tags = (beanAnnotation?.arguments?.firstOrNull {
                it.name?.asString() == "tags"
            }?.value as? List<String>) ?: emptyList()
            return Triple(name, order, tags)
        }

        private fun extractClassInfo(clazz: KSClassDeclaration, resolver: Resolver, beanName: String, order: Int, tags: List<String>) {
            val lsiClass = clazz.toLsiClass(resolver)
            if (!lsiClass.hasNoArgConstructor) return
            val qualifiedName = lsiClass.qualifiedName ?: return
            val name = beanName.ifEmpty { lsiClass.name?.replaceFirstChar { it.lowercase() } ?: "" }
            beans.add(BeanInfo(qualifiedName, InitType.CLASS_INSTANCE, order, name, tags))
            classComponents.add(lsiClass)
        }

        private fun extractObjectInfo(obj: KSClassDeclaration, resolver: Resolver, beanName: String, order: Int, tags: List<String>) {
            val lsiClass = obj.toLsiClass(resolver)
            val qualifiedName = lsiClass.qualifiedName ?: return
            val name = beanName.ifEmpty { lsiClass.name?.replaceFirstChar { it.lowercase() } ?: "" }
            beans.add(BeanInfo(qualifiedName, InitType.OBJECT_INSTANCE, order, name, tags))
        }

        private fun extractFunctionInfo(function: KSFunctionDeclaration, resolver: Resolver) {
            val lsiMethod = function.toLsiMethod(resolver)
            if (!lsiMethod.hasNoRequiredParameters) return

            val parentClass = lsiMethod.parentClass
            val packageName = function.packageName.asString().takeIf { it.isNotEmpty() }
            val (beanName, order, tags) = extractBeanAnnotation(function)
            val isComposable = lsiMethod.isComposable
            val isSuspend = lsiMethod.isSuspend

            if (isComposable && isSuspend) {
                environment.logger.error(
                    "@Bean function '${function.simpleName.asString()}' cannot be both @Composable and suspend",
                    function
                )
                return
            }

            val isExtension = function.extensionReceiver != null

            val initType = when {
                isExtension && parentClass == null -> InitType.EXTENSION_FUNCTION
                parentClass == null && isComposable -> InitType.COMPOSABLE_FUNCTION
                parentClass == null && isSuspend -> InitType.SUSPEND_FUNCTION
                parentClass == null -> InitType.TOP_LEVEL_FUNCTION
                parentClass.isCompanionObject -> InitType.COMPANION_OBJECT
                else -> InitType.CLASS_INSTANCE
            }

            val fullName = when (initType) {
                InitType.EXTENSION_FUNCTION -> {
                    val receiverType = function.extensionReceiver!!.resolve()
                        .declaration.qualifiedName?.asString() ?: ""
                    val funcName = function.simpleName.asString()
                    val funcPackage = packageName ?: ""
                    "$receiverType::$funcPackage.$funcName"
                }
                InitType.COMPOSABLE_FUNCTION, InitType.SUSPEND_FUNCTION, InitType.TOP_LEVEL_FUNCTION -> {
                    val name = lsiMethod.name
                    if (packageName != null && name != null) "$packageName.$name" else name ?: ""
                }
                else -> {
                    parentClass?.qualifiedName ?: ""
                }
            }

            beans.add(BeanInfo(fullName, initType, order, beanName, tags))
        }

        override fun process(resolver: Resolver): List<KSAnnotated> {
            // @Bean on functions
            resolver.getSymbolsWithAnnotation(Bean::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .forEach { extractFunctionInfo(it, resolver) }

            // @Bean on classes
            resolver.getSymbolsWithAnnotation(Bean::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.CLASS }
                .forEach {
                    val (name, order, tags) = extractBeanAnnotation(it)
                    extractClassInfo(it, resolver, name, order, tags)
                }

            // @Bean on objects
            resolver.getSymbolsWithAnnotation(Bean::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.OBJECT }
                .forEach {
                    val (name, order, tags) = extractBeanAnnotation(it)
                    extractObjectInfo(it, resolver, name, order, tags)
                }

            return emptyList()
        }

        override fun finish() {
            if (beans.isEmpty()) return

            val sortedBeans = beans.sortedBy { it.order }
            val codeGenerator = environment.codeGenerator

            // derive generated package: explicit option > first non-extension bean's package > fallback
            val generatedPackage = modulePackage
                ?: sortedBeans.firstOrNull { !it.name.contains("::") }
                    ?.name?.substringBeforeLast(".")
                    ?.let { "$it.ioc.generated" }
                ?: sortedBeans.first().name.substringAfter("::")
                    .substringBeforeLast(".")
                    .let { "$it.ioc.generated" }

            ContainerGenerator(codeGenerator, generatedPackage, isApp)
                .generate(sortedBeans, classComponents)
        }
    }
}

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
import site.addzero.ioc.annotation.Component
import site.addzero.ioc.annotation.ComponentScan
import site.addzero.ioc.container.ContainerGenerator
import site.addzero.ioc.registry.RegistryGenerator
import site.addzero.ioc.scanner.ComponentScanGenerator
import site.addzero.ioc.scanner.ComponentScanInfo
import site.addzero.ioc.strategy.BeanInfo
import site.addzero.ioc.strategy.InitType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.hasNoArgConstructor
import site.addzero.util.lsi.method.hasNoRequiredParameters
import site.addzero.util.lsi.method.isComposable
import site.addzero.util.lsi.method.parentClass
import site.addzero.util.lsi_impl.impl.ksp.toLsiClass
import site.addzero.util.lsi_impl.impl.ksp.toLsiMethod

class IocProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = object : SymbolProcessor {
        private val beans = mutableListOf<BeanInfo>()
        private val components = mutableListOf<LsiClass>()
        private val componentScans = mutableListOf<ComponentScanInfo>()

        private fun extractOrder(annotated: KSAnnotated): Int {
            val beanAnnotation = annotated.annotations.firstOrNull {
                it.shortName.asString() == "Bean"
            } ?: return 0
            return beanAnnotation.arguments.firstOrNull {
                it.name?.asString() == "order"
            }?.value as? Int ?: 0
        }

        private fun extractClassInfo(clazz: KSClassDeclaration, resolver: Resolver) {
            val lsiClass = clazz.toLsiClass(resolver)
            if (!lsiClass.hasNoArgConstructor) return
            val order = extractOrder(clazz)
            lsiClass.qualifiedName?.let { beans.add(BeanInfo(it, InitType.CLASS_INSTANCE, order)) }
        }

        private fun extractObjectInfo(obj: KSClassDeclaration, resolver: Resolver) {
            val lsiClass = obj.toLsiClass(resolver)
            val order = extractOrder(obj)
            lsiClass.qualifiedName?.let { beans.add(BeanInfo(it, InitType.OBJECT_INSTANCE, order)) }
        }

        private fun extractFunctionInfo(function: KSFunctionDeclaration, resolver: Resolver) {
            val lsiMethod = function.toLsiMethod(resolver)
            if (!lsiMethod.hasNoRequiredParameters) return

            val parentClass = lsiMethod.parentClass
            val packageName = function.packageName.asString().takeIf { it.isNotEmpty() }
            val order = extractOrder(function)

            val isExtension = function.extensionReceiver != null

            val initType = when {
                isExtension && parentClass == null -> InitType.EXTENSION_FUNCTION
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
                InitType.TOP_LEVEL_FUNCTION -> {
                    val name = if (lsiMethod.isComposable) {
                        lsiMethod.name?.replaceFirstChar { it.uppercase() }
                    } else lsiMethod.name
                    if (packageName != null && name != null) "$packageName.$name" else name ?: ""
                }
                else -> {
                    parentClass?.qualifiedName ?: ""
                }
            }

            beans.add(BeanInfo(fullName, initType, order))
        }

        override fun process(resolver: Resolver): List<KSAnnotated> {
            resolver.getSymbolsWithAnnotation(Bean::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .forEach { extractFunctionInfo(it, resolver) }

            resolver.getSymbolsWithAnnotation(Bean::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.CLASS }
                .forEach { extractClassInfo(it, resolver) }

            resolver.getSymbolsWithAnnotation(Bean::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.OBJECT }
                .forEach { extractObjectInfo(it, resolver) }

            resolver.getSymbolsWithAnnotation(Component::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.CLASS }
                .forEach { components.add(it.toLsiClass(resolver)) }

            resolver.getSymbolsWithAnnotation(ComponentScan::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .forEach { declaration ->
                    val lsiClass = declaration.toLsiClass(resolver)
                    val annotation = lsiClass.annotations.find {
                        it.qualifiedName == "site.addzero.ioc.annotation.ComponentScan"
                    }

                    val packages = (annotation?.getAttribute("packages") as? Array<*>)
                        ?.mapNotNull { it as? String } ?: emptyList()
                    val excludePackages = (annotation?.getAttribute("excludePackages") as? Array<*>)
                        ?.mapNotNull { it as? String } ?: emptyList()
                    val defaultNamespace = annotation?.getAttribute("defaultNamespace") as? String
                        ?: "site.addzero.ioc.metadata"

                    componentScans.add(ComponentScanInfo(lsiClass, packages, excludePackages, defaultNamespace))
                }

            return emptyList()
        }

        override fun finish() {
            // sort by order before generating
            val sortedBeans = beans.sortedBy { it.order }

            val codeGenerator = environment.codeGenerator
            ContainerGenerator(codeGenerator).generate(sortedBeans)
            RegistryGenerator(codeGenerator).apply {
                generate(components)
                generateMetadata(components)
            }
            ComponentScanGenerator(codeGenerator).generate(componentScans, components)
        }
    }
}

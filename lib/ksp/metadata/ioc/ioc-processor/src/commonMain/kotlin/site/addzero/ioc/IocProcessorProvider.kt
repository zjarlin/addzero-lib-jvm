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
        private val functions = mutableListOf<Pair<String, InitType>>()
        private val components = mutableListOf<LsiClass>()
        private val componentScans = mutableListOf<ComponentScanInfo>()

        private fun extractClassInfo(clazz: KSClassDeclaration, resolver: Resolver) {
            val lsiClass = clazz.toLsiClass(resolver)
            if (!lsiClass.hasNoArgConstructor) return
            lsiClass.qualifiedName?.let { functions.add(it to InitType.CLASS_INSTANCE) }
        }

        private fun extractObjectInfo(obj: KSClassDeclaration, resolver: Resolver) {
            val lsiClass = obj.toLsiClass(resolver)
            lsiClass.qualifiedName?.let { functions.add(it to InitType.OBJECT_INSTANCE) }
        }

        private fun extractFunctionInfo(function: KSFunctionDeclaration, resolver: Resolver) {
            val lsiMethod = function.toLsiMethod(resolver)
            if (!lsiMethod.hasNoRequiredParameters) return

            val parentClass = lsiMethod.parentClass
            val packageName = function.packageName.asString().takeIf { it.isNotEmpty() }
            val initType = when {
                parentClass == null -> InitType.TOP_LEVEL_FUNCTION
                parentClass.isCompanionObject -> InitType.COMPANION_OBJECT
                else -> InitType.CLASS_INSTANCE
            }

            val fullName = if (initType == InitType.TOP_LEVEL_FUNCTION) {
                val name = if (lsiMethod.isComposable) {
                    lsiMethod.name?.replaceFirstChar { it.uppercase() }
                } else lsiMethod.name
                if (packageName != null && name != null) "$packageName.$name" else name ?: ""
            } else {
                parentClass?.qualifiedName ?: ""
            }

            functions.add(fullName to initType)
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
                    val defaultNamespace = annotation?.getAttribute("defaultNamespace") as? String
                        ?: "site.addzero.ioc.metadata"

                    componentScans.add(ComponentScanInfo(lsiClass, packages, defaultNamespace))
                }

            return emptyList()
        }

        override fun finish() {
            val codeGenerator = environment.codeGenerator
            ContainerGenerator(codeGenerator).generate(functions)
            RegistryGenerator(codeGenerator).apply {
                generate(components)
                generateMetadata(components)
            }
            ComponentScanGenerator(codeGenerator).generate(componentScans, components)
        }
    }
}
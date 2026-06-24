package site.addzero.jimmer.ddl.compiler.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import site.addzero.jimmer.ddl.compiler.JimmerDdlCompiler
import site.addzero.jimmer.ddl.compiler.JimmerDdlCompilerFiles
import site.addzero.jimmer.ddl.compiler.JimmerDdlCompilerSettings
import site.addzero.jimmer.ddl.compiler.toStableJimmerDdlSnapshot
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.ksp.clazz.toLsiClass

private const val JIMMER_ENTITY = "org.babyfish.jimmer.sql.Entity"

class JimmerDdlCompilerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val settingsList = JimmerDdlCompilerSettings.allFromOptions(environment.options)
        return object : SymbolProcessor {
            private val entities = linkedMapOf<String, LsiClass>()
            private var hasErrors = false

            override fun process(resolver: Resolver): List<KSAnnotated> {
                if (settingsList.none { it.enabled }) {
                    return emptyList()
                }
                val deferred = mutableListOf<KSAnnotated>()
                resolver.getSymbolsWithAnnotation(JIMMER_ENTITY).forEach { symbol ->
                    if (!symbol.validate()) {
                        deferred += symbol
                        return@forEach
                    }
                    val declaration = symbol as? KSClassDeclaration
                    if (declaration == null) {
                        environment.logger.error("Jimmer DDL 只能处理类声明: $symbol", symbol)
                        hasErrors = true
                        return@forEach
                    }
                    val key = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
                    entities[key] = declaration.toLsiClass(resolver).toStableJimmerDdlSnapshot()
                }
                return deferred
            }

            override fun finish() {
                if (hasErrors || entities.isEmpty()) {
                    return
                }
                settingsList.forEach { settings ->
                    val result = JimmerDdlCompiler.compile(entities.values, settings)
                    if (!result.isEmpty) {
                        val outputFile = JimmerDdlCompilerFiles.writeOutputFile(settings, result.sql)
                        environment.logger.warn("Jimmer DDL 已生成: ${outputFile.absolutePath}")
                    }
                }
            }
        }
    }
}

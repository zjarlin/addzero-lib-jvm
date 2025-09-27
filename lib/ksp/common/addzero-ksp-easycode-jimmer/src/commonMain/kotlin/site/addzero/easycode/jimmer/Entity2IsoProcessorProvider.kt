package site.addzero.easycode.jimmer

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import site.addzero.easycode.inter.AbsCodegenContext
import site.addzero.easycode.inter.MetadataContext
import site.addzero.easycode.inter.TemplateContext
import site.addzero.easycode.ksp.AbstractTemplateProcessor
import site.addzero.entity.analysis.analyzer.JimmerEntityAnalyzer
import site.addzero.entity.analysis.model.EntityMetadata
import kotlin.reflect.KClass

val entitysoContext = object : AbsCodegenContext<EntityMetadata> {
    override val clazz: KClass<EntityMetadata>
        get() = EntityMetadata::class

    override fun extract(resolver: Resolver): MetadataContext<EntityMetadata> {
        val entitySymbols = resolver
            .getSymbolsWithAnnotation("org.babyfish.jimmer.sql.Entity")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        val dependencies = entitySymbols.mapNotNull { it.containingFile }

        val jimmerEntityAnalyzer = JimmerEntityAnalyzer()
        val metadatas = entitySymbols.map {
            val analyzeEntity = jimmerEntityAnalyzer.analyzeEntity(it)
            analyzeEntity
        }
        return MetadataContext(
            metadata = metadatas.toList(),
            dependencies = dependencies.toList(),
            notValid = dependencies.filterNot { it.validate() }.toList()
        )
    }

    override val templateContext: List<TemplateContext<EntityMetadata>>
        get() = emptyList<TemplateContext<EntityMetadata>>()
}









class JimmerEntityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {

        return object : AbstractTemplateProcessor<EntityMetadata>(
            entitysoContext,
            environment
        ) {

        }


    }
}


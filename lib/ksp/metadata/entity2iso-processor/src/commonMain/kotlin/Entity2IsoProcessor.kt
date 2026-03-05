import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import generator.IsoCodeGenerator
import java.io.File
import site.addzero.entity2iso.processor.context.Settings
import site.addzero.lsi.ksp.clazz.toLsiClass

/**
 * 实体转同构体处理器提供者
 */
class Entity2IsoProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Entity2IsoProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}

/**
 * 实体转同构体处理器
 *
 * 专门负责生成同构体类，被 shared 模块依赖
 * 基于 LSI（Language Structure Interface）读取实体结构并生成同构体
 *
 * 生成目录：shared/src/commonMain/kotlin/site/addzero/kmp/isomorphic/
 */
class Entity2IsoProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private companion object {
        const val JIMMER_ENTITY_ANNOTATION = "org.babyfish.jimmer.sql.Entity"
    }

    // 同构体代码生成器
    private val isoCodeGenerator = IsoCodeGenerator(logger)

    // 跟踪已生成的同构体，避免重复生成
    private val generatedIsoClasses = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        Settings.fromOptions(options)

        val packageName = Settings.isomorphicPkg
        val outputDir = Settings.isomorphicGenDir
        val outputDirFile = File(outputDir).also { it.mkdirs() }

        val symbols = resolver.getSymbolsWithAnnotation(JIMMER_ENTITY_ANNOTATION)
        val deferred = symbols.filterNot { it.validate() }.toList()

        val entities = symbols
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (entities.isNotEmpty()) {
            logger.warn("开始生成同构体类...")
        }

        entities.forEach { entity ->
            val entityQualifiedName = entity.qualifiedName?.asString() ?: return@forEach
            val entitySimpleName = entity.simpleName.asString()

            try {
                if (!generatedIsoClasses.add(entityQualifiedName)) {
                    logger.info("跳过已生成的同构体: ${entitySimpleName}Iso")
                    return@forEach
                }

                val lsiClass = entity.toLsiClass(resolver)
                val isoCode = isoCodeGenerator.generateIsoCode(lsiClass, packageName)

                val fileName = "${entitySimpleName}Iso.kt"
                val file = File(outputDirFile, fileName)
                file.writeText(isoCode)

                logger.info("生成同构体: ${entitySimpleName}Iso")
            } catch (e: Exception) {
                logger.error("生成同构体失败: $entitySimpleName, 错误: ${e.message}")
            }
        }

        if (entities.isNotEmpty()) {
            logger.warn("同构体类生成完成，共生成 ${generatedIsoClasses.size} 个")
        }

        return deferred
    }
}

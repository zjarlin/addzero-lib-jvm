package site.addzero.kcloud.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import site.addzero.kcloud.codegen.ir.ParseResult

class OpenApiCodegenProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    companion object {
        private const val OPTION_SPEC = "openapi.spec"
        private const val OPTION_PACKAGE = "openapi.package"
        private const val DEFAULT_PACKAGE = "generated.api"
    }

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 只在第一轮处理，后续轮次直接返回
        if (processed) return emptyList()
        processed = true

        val specPath = options[OPTION_SPEC]
        if (specPath.isNullOrBlank()) {
            logger.error("Missing required KSP argument: openapi.spec. Use ksp { arg(\"openapi.spec\", \"path/to/spec.json\") } in build.gradle.kts.")
            return emptyList()
        }

        val packageName = options[OPTION_PACKAGE]?.takeIf { it.isNotBlank() } ?: DEFAULT_PACKAGE
        logger.info("OpenAPI codegen: spec=$specPath, package=$packageName")

        // 1. 解析 OpenAPI 规范文件
        val parseResult = SchemaParser().parse(specPath)

        when (parseResult) {
            is ParseResult.Failure -> {
                for (error in parseResult.errors) {
                    logger.error("OpenAPI parse error: $error")
                }
            }
            is ParseResult.Success -> {
                val ir = parseResult.ir
                logger.info("OpenAPI parsed: ${ir.interfaces.size} interfaces, ${ir.models.size} models")

                // 2. 生成 Kotlin 源文件
                val emitter = CodeEmitter(packageName)
                val fileSpecs = emitter.emit(ir)

                // 3. 将生成的文件写入 KSP CodeGenerator
                for (fileSpec in fileSpecs) {
                    val outputStream = codeGenerator.createNewFile(
                        dependencies = Dependencies(aggregating = false),
                        packageName = fileSpec.packageName,
                        fileName = fileSpec.name
                    )
                    outputStream.use { os ->
                        os.writer().use { writer ->
                            fileSpec.writeTo(writer)
                        }
                    }
                }

                logger.info("OpenAPI codegen: generated ${fileSpecs.size} files")
            }
        }

        return emptyList()
    }
}

package site.addzero.easycode.ksp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import site.addzero.easycode.inter.AbsCodegenContext
import site.addzero.util.genCode
import site.addzero.util.str.withFileName
import site.addzero.util.str.withFileSuffix
import site.addzero.util.str.withPkg
import java.io.File
import java.nio.file.Files

abstract class AbstractTemplateProcessor<T : Any>(
    protected val absCodegenContext: AbsCodegenContext<T>,
    protected val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    protected val options = environment.options
    protected val codeGenerator = environment.codeGenerator
    //velocity 模板引擎

    private var metadataCache = null as List<T>
    private var dependenciesFiles = mutableSetOf<KSFile>()
    override fun process(resolver: Resolver): List<KSAnnotated> {


        val extract = absCodegenContext.extract(resolver)
        metadataCache = extract.metadata
        val dependencies = extract.dependencies
        dependenciesFiles.addAll(dependencies)
        return dependencies
    }

    override fun finish() {

        val templateContext = absCodegenContext.templateContext
        val clazz = absCodegenContext.clazz
        templateContext.forEach {
            val metadata = metadataCache
            val templatePath = it.templatePath
            val skipExistFile = it.skipExistFile
            val templateConent = Files.readString(File(templatePath).toPath())

            metadata.forEach { meta ->
                val fileName = it.getFileName(meta, options)
                val withPkg = it.getRelativePath.withPkg(it.getPkg)
                val pathName = withPkg.withFileName(fileName).withFileSuffix(it.getFileSuffix)

                val formatCode: String = VelocityUtil.formatCode(
                    templateConent = templateConent,
                    meta = meta,
                    kspOption = options,
                    kclass = clazz
                ) {}

                if (!it.useKspCodeGenerator) {
                    genCode(
                        pathname = pathName,
                        code = formatCode,
                        skipExistFile = skipExistFile
                    )
                } else {
                    val fileNameWithSuffix = fileName.withFileSuffix(it.getFileSuffix)
                    codeGenerator.createNewFile(
                        dependencies = Dependencies(true, *dependenciesFiles.toTypedArray()),
                        packageName = it.getPkg,
                        fileName = fileNameWithSuffix
                    ).use { stream ->
                        stream.write(formatCode.toByteArray())
                    }

                }

            }


        }

    }
}


package com.addzero.kmp.kaleidoscope.codegen.pureksp

import com.addzero.kmp.kaleidoscope.codegen.VelocityTemplateEngine
import com.addzero.kmp.kaleidoscope.codegen.pureksp.jimmer.TemlateContext
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate


typealias Ret = MutableMap<String, Any?>

/**
 * 纯KSP实现的代码生成器基类
 *
 * 提供类似IDEA Easy Code的分组和多模板功能：
 * 1. 支持多个模板组
 * 2. 每个组下支持多个模板
 * 3. 支持Velocity宏的文件命名
 * 4. 统一的元数据抽取和代码生成流程
 *
 * @param T 元数据类型
 */
abstract class VlProcessor(
    protected val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    protected val logger = environment.logger
    protected val templateEngine = VelocityTemplateEngine(environment)

    protected val options = environment.options



    /**
     * 将元数据对象T转换为Velocity模板可用的Map
     */
    protected abstract fun collectRet(resolver: Resolver): Sequence<Ret>


    /**
     * 将元数据对象T转换为Velocity模板可用的Map
     */
    protected abstract fun getTemplates(): List<TemlateContext>

    /**
     * 存储收集到的元数据
     */
    protected val collectedMetadata = mutableSetOf<Ret>()


    override fun process(resolver: Resolver): List<KSAnnotated> {
        val collectRet = collectRet(resolver)
        collectedMetadata.addAll(collectRet)
        val filterNot = resolver.getAllFiles().filterNot { it.validate() }.toList()
        return filterNot
    }


    override fun finish() {
        if (collectedMetadata.isEmpty()) {
            logger.warn("没有收集到元数据，跳过代码生成")
            return
        }

        val string = options["serverSourceDir"]
        val string1 = options["serverBuildDir"]
        val string2 = options["composeSourceDir"]
        val string3 = options["composeBuildDir"]
        val string4 = options["sharedSourceDir"]
        val string5 = options["sharedBuildDir"]
        val string6 = options["modelSourceDir"]
        val string7 = options["modelBuildDir"]




        val templates = getTemplates()



        templates.forEach { temlateContext ->
            val path = temlateContext.templatePath
            temlateContext.fileNamePattern

            collectedMetadata.forEach { metadata ->
                gencode(temlateContext, metadata)


            }


        }


    }

    fun gencode(temlateContext: TemlateContext, metadata: Ret) {
        val templatePath = temlateContext.templatePath
        val readContent:String = readContent(templatePath)
        val vlprocessTemplate = templateEngine.vlprocessTemplate(readContent, metadata)

    }

    fun readContent(templatePath: String): String {
        TODO("Not yet implemented")
    }


}

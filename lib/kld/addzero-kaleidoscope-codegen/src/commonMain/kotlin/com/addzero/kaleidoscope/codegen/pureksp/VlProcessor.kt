package com.addzero.kaleidoscope.codegen.pureksp

import com.addzero.kaleidoscope.codegen.VelocityTemplateEngine
import com.addzero.kaleidoscope.codegen.pureksp.jimmer.TemlateContext
import com.addzero.kaleidoscope.core.KldResolver
import com.addzero.kaleidoscope.util.CodeUtil
import com.addzero.util.str.withFileName
import com.addzero.util.str.withFileSuffix
import com.addzero.util.str.withPkg
import java.io.File

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
abstract class KldCodeGenerator() {


    /**
     * 将元数据对象T转换为Velocity模板可用的Map
     */
    abstract fun singleFileFlag(): Boolean



    /**
     * 将元数据对象T转换为Velocity模板可用的Map
     */
    abstract fun collectRet(resolver: KldResolver): Sequence<Ret>


    /**
     * 将元数据对象T转换为Velocity模板可用的Map
     */
    abstract fun getTemplateContexts(): List<TemlateContext>

    /**
     * 存储收集到的元数据
     */
    protected val collectedMetadata = mutableSetOf<Ret>()


    fun finish() {
        println("tttttt收集到的元数据有：${collectedMetadata}")
        if (collectedMetadata.isEmpty()) {
            return
        }
//        list往单模板里塞
//        t往模板里塞

        val templates = getTemplateContexts()
        templates.forEach { temlateContext ->
            val path = temlateContext.templatePath
            temlateContext.fileNamePattern


            if (singleFileFlag()) {
                collectedMetadata.forEach { metadata ->
                    gencode(temlateContext, metadata)
                }
            } else {
                val mapOf = mapOf("ret" to collectedMetadata) as Map<String, Any?>
                gencode(temlateContext, mapOf)
            }
        }
    }
    fun gencode(temlateContext: TemlateContext, metadata: Map<String, Any?>) {
        val templatePath = temlateContext.templatePath
        val readContent: String = readContent(templatePath)
        val generatedCode = VelocityTemplateEngine.vlprocessTemplate(readContent, metadata)

        // 生成文件名和包名
        val fileName = VelocityTemplateEngine.vlformat(temlateContext.fileNamePattern, metadata)
        val packageName = VelocityTemplateEngine.vlformat(temlateContext.pkgPattern, metadata)

        // 移除文件扩展名（如果有的话）
        val fileNameWithoutExtension = fileName.substringBeforeLast(".")
        CodeUtil.genCode(temlateContext.outputDir.withPkg(packageName).withFileName(fileName).withFileSuffix(), generatedCode)

    }

    fun readContent(templatePath: String): String {
//        FileU
        val readWithUse = File(templatePath).reader().use { it.readText() }
        return readWithUse

    }
}

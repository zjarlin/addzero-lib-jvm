package com.addzero.kaleidoscope.codegen

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import java.io.StringWriter
import java.time.LocalDateTime
import kotlin.jvm.java

/**
 * Velocity模板引擎实现
 *
 * 使用Apache Velocity引擎提供完整的模板处理功能
 */
class VelocityTemplateEngine(
    private val environment: SymbolProcessorEnvironment
) {
    private val logger = environment.logger
    private val velocityEngine: VelocityEngine

    init {
        // 初始化Velocity引擎
        velocityEngine = VelocityEngine()
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        velocityEngine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8")
        velocityEngine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8")
        velocityEngine.init()
    }

    /**
     * 处理模板并生成代码
     *
     * @param this@processTemplate 模板路径
     * @param context 模板上下文数据
     * @return 生成的代码
     */
    fun vlprocessTemplate(string: String, context: Map<String, Any?>): String {
        return try {
            val template = velocityEngine.getTemplate(string)
            val velocityContext = VelocityContext()

            // 将上下文数据添加到Velocity上下文中
            context.forEach { (key, value) ->
                velocityContext.put(key, value)
            }

            // 添加当前时间戳
            velocityContext.put(".now", LocalDateTime.now().toString())

            val writer = StringWriter()
            template.merge(velocityContext, writer)
            writer.toString()
        } catch (e: Exception) {
            logger.error("处理模板时发生错误: ${e.message}")
            "// 模板处理错误: ${e.message}\n"
        }
    }

    /**
     * 处理模板字符串内容并生成代码
     *
     * @param this@format 模板内容
     * @param context 模板上下文数据
     * @return 生成的代码
     */
    fun vlformat(string: String, context: Map<String, Any?>): String {
        return try {
            val velocityContext = VelocityContext()
            // 将上下文数据添加到Velocity上下文中
            context.forEach { (key, value) ->
                velocityContext.put(key, value)
            }
            // 添加当前时间戳
            velocityContext.put(".now", LocalDateTime.now().toString())
            val writer = StringWriter()
            velocityEngine.evaluate(velocityContext, writer, "inline-template", string)
            writer.toString()
        } catch (e: Exception) {
            logger.error("处理模板内容时发生错误: ${e.message}")
            "// 模板处理错误: ${e.message}\n"
        }
    }
}

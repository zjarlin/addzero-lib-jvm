package com.addzero.kmp.codegen.core

/**
 * 代码生成配置
 * 
 * 包含模板引擎和代码生成所需的配置信息
 */
data class CodeGenConfig(
    /**
     * 模板目录路径
     * 可以是类路径下的资源目录或文件系统路径
     */
    val templateDirectory: String,
    
    /**
     * 输出目录路径
     * 生成的代码文件将写入此目录
     */
    val outputDirectory: String,
    
    /**
     * 是否覆盖已存在的文件
     * true: 覆盖已存在的文件
     * false: 跳过已存在的文件
     */
    val overwriteExisting: Boolean = false,
    
    /**
     * Velocity引擎配置
     * 可以自定义Velocity引擎的行为
     */
    val velocityProperties: Map<String, Any> = defaultVelocityProperties(),
    
    /**
     * 是否启用调试模式
     * 调试模式下会输出更多日志信息
     */
    val debugMode: Boolean = false,
    
    /**
     * 文件编码
     */
    val encoding: String = "UTF-8"
) {
    
    companion object {
        /**
         * 默认的Velocity引擎配置
         */
        fun defaultVelocityProperties(): Map<String, Any> = mapOf(
            "resource.loader" to "classpath",
            "classpath.resource.loader.class" to "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
            "input.encoding" to "UTF-8",
            "output.encoding" to "UTF-8",
            "directive.set.null.allowed" to true,
            "velocimacro.permissions.allow.inline.to.replace.global" to true
        )
        
        /**
         * 从KSP参数创建配置
         */
        fun fromKspOptions(options: Map<String, String>): CodeGenConfig {
            return CodeGenConfig(
                templateDirectory = options["templateDir"] ?: "templates",
                outputDirectory = options["outputDir"] ?: "generated/ksp/main/kotlin",
                overwriteExisting = options["overwrite"]?.toBoolean() ?: false,
                debugMode = options["debug"]?.toBoolean() ?: false,
                encoding = options["encoding"] ?: "UTF-8"
            )
        }
    }
}
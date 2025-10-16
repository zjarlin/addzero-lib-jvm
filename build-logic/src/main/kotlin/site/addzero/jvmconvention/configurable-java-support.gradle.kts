package site.addzero.jvmconvention

import site.addzero.gradle.configureJava

plugins {
    `java-library`
}

// 创建扩展来接收用户配置
extensions.create<JavaSupportExtension>("javaSupport")

interface JavaSupportExtension {
    /** Java版本，默认为8 */
    val version: Property<Int>
}

// 设置默认版本为8
val javaSupport = the<JavaSupportExtension>()
javaSupport.version.convention(8)

// 配置Java版本
afterEvaluate {
    val javaVersion = javaSupport.version.get()

    // 使用工具函数配置Java
    configureJava(javaVersion)

    logger.lifecycle("Configured Java version: $javaVersion for project: $name")
}

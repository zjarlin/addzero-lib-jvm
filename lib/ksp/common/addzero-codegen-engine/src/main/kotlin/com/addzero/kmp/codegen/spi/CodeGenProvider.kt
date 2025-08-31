package com.addzero.kmp.codegen.spi

import com.addzero.kmp.codegen.core.*

/**
 * 代码生成提供者接口
 *
 * 通过枚举实现编译时SPI机制，用户只需要在自己的模块中
 * 添加枚举项和对应的模板文件即可扩展代码生成功能
 *
 * @param T 元数据类型
 */
interface CodeGenProvider<T> {

    /**
     * 提供者唯一标识
     */
    val providerId: String

    /**
     * 提供者描述
     */
    val description: String

    /**
     * 是否启用此提供者
     */
    val enabled: Boolean get() = true

    /**
     * 优先级，数字越小优先级越高
     */
    val priority: Int get() = 100

    /**
     * 创建元数据提取器
     */
    fun createMetadataExtractor(): MetadataExtractor<T>

    /**
     * 创建文件生成器列表
     * 支持一个提供者生成多个文件
     */
    fun createFileGenerators(): List<FileGenerator<T>>

    /**
     * 检查是否应该处理此提供者
     * 可以基于KSP选项、环境变量等进行判断
     */
    fun shouldProcess(options: Map<String, String>): Boolean = enabled
}

/**
 * 代码生成提供者枚举接口
 *
 * 用户需要实现此接口来定义自己的代码生成提供者枚举
 * 枚举的每一项代表一个代码生成场景
 */
interface CodeGenProviderEnum<T> {
    /**
     * 获取代码生成提供者实例
     */
    fun getProvider(): CodeGenProvider<T>
}

/**
 * 提供者注册器
 *
 * 负责从枚举中收集所有的代码生成提供者
 */
object ProviderRegistry {

    /**
     * 从枚举类中提取所有提供者
     *
     * @param enumClass 实现了CodeGenProviderEnum的枚举类
     * @return 按优先级排序的提供者列表
     */
    inline fun <reified E, T> extractProviders(enumClass: Class<E>): List<CodeGenProvider<T>>
            where E : Enum<E>, E : CodeGenProviderEnum<T> {

        return enumClass.enumConstants
            .map { it.getProvider() }
            .filter { it.enabled }
            .sortedBy { it.priority }
    }

    /**
     * 从枚举类中提取启用的提供者
     */
    inline fun <reified E, T> extractEnabledProviders(
        options: Map<String, String>
    ): List<CodeGenProvider<T>>
            where E : Enum<E>, E : CodeGenProviderEnum<T> {


        return extractProviders<E, T>(E::class.java)
            .filter { it.shouldProcess(options) }
    }
}

/**
 * 抽象代码生成提供者
 *
 * 提供一些常用的默认实现
 */
abstract class AbstractCodeGenProvider<T> : CodeGenProvider<T> {

    override val enabled: Boolean = true
    override val priority: Int = 100

    override fun shouldProcess(options: Map<String, String>): Boolean {
        // 检查是否通过选项禁用了此提供者
        val disabledProviders = options["disabled.providers"]
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

        return enabled && providerId !in disabledProviders
    }
}

/**
 * 简单的代码生成提供者实现
 *
 * 适用于只需要一个文件生成器的简单场景
 */
abstract class SimpleCodeGenProvider<T> : AbstractCodeGenProvider<T>() {

    /**
     * 创建单个文件生成器
     */
    abstract fun createFileGenerator(): FileGenerator<T>

    override fun createFileGenerators(): List<FileGenerator<T>> {
        return listOf(createFileGenerator())
    }
}

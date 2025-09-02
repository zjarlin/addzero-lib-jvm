package com.addzero.kaleidoscope

import com.addzero.kaleidoscope.core.KldResolver

/**
 * Kaleidoscope 处理器接口
 *
 * 这是 Kaleidoscope 代码生成框架的核心接口，用于定义代码生成处理器。
 * 该接口为 KSP 和 APT 提供了统一的抽象。
 */
interface KldProcessor {

    /**
     * 处理代码元素
     *
     * @param resolver KldResolver 实例，用于获取代码元素信息
     * @param environment KldEnvironment 实例，提供处理器环境信息
     * @return 是否需要继续处理
     */
    fun process(resolver: KldResolver, environment: KldEnvironment): Boolean

    /**
     * 处理完成后的回调
     *
     * @param resolver KldResolver 实例
     * @param environment KldEnvironment 实例，提供处理器环境信息
     */
    fun finish(resolver: KldResolver, environment: KldEnvironment)
}
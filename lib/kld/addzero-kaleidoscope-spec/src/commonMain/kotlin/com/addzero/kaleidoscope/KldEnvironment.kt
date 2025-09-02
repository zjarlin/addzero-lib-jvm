package com.addzero.kaleidoscope

import com.addzero.kaleidoscope.core.KldLogger
import com.addzero.kaleidoscope.core.KldWriter

/**
 * Kaleidoscope 处理器环境接口
 *
 * 这是 Kaleidoscope 代码生成框架的环境接口，用于为处理器提供运行时环境信息。
 * 该接口为 KSP 和 APT 提供了统一的抽象。
 */
interface KldEnvironment {

    /**
     * 获取处理器选项
     *
     * @return 处理器选项的键值对映射
     */
    val options: Map<String, String>

    val logger: KldLogger
    /**
     * 创建源文件
     *
     * @param packageName 包名
     * @param fileName 文件名（不包含扩展名）
     * @param originatingElements 源元素
     * @return KldWriter 实例，用于写入文件内容
     */
    fun createSourceFile(
        packageName: String,
        fileName: String,
        vararg originatingElements: Any
    ): KldWriter

}

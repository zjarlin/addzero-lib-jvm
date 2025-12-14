package site.addzero.apt.dict.processor.unified.adapter

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.context.LsiContext
import site.addzero.util.lsi_impl.impl.apt.clazz.AptLsiClass
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * APT到LSI的适配器
 *
 * 负责将APT的元素转换为LSI抽象，提供统一的元编程接口
 */
class AptLsiAdapter(
    private val elements: Elements,
    private val types: Types
) {

    /**
     * 创建LSI上下文
     */
    fun createLsiContext(
        currentClass: LsiClass?,
        filePath: String? = null
    ): LsiContext {
        return LsiContext(
            currentClass = currentClass,
            currentFile = null, // APT环境下通常不需要文件信息
            filePath = filePath,
            allClassesInFile = currentClass?.let { listOf(it) } ?: emptyList()
        )
    }


}

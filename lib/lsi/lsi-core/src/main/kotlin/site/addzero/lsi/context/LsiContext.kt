package site.addzero.lsi.context

import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.file.LsiFile

/**
 * LSI上下文 - 语言无关的编辑器上下文抽象
 *
 * 封装当前编辑器中的类、文件等元信息
 * 遵循单一职责原则，只负责提供当前编辑上下文
 */
data class LsiContext(
    /**
     * 当前焦点的类（可能为空）
     */
    val currentClass: LsiClass?,

    /**
     * 当前打开的文件
     */
    val currentFile: LsiFile?,

    /**
     * 文件路径
     */
    val filePath: String?,

    /**
     * 文件中的所有类（用于处理一个文件包含多个类的情况）
     */
    val allClassesInFile: List<LsiClass> = emptyList()
) {
    /**
     * 是否有有效的类上下文
     */
    val hasValidClass: Boolean
        get() = currentClass != null

    /**
     * 是否有有效的文件上下文
     */
    @Suppress("unused")
    val hasValidFile: Boolean
        get() = currentFile != null

    companion object {
        /**
         * 空上下文
         */
        val EMPTY = LsiContext(
            currentClass = null,
            currentFile = null,
            filePath = null,
            allClassesInFile = emptyList()
        )
    }
}

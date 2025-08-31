package com.addzero.kaleidoscope.core

/**
 * 跨平台文件写入接口
 */
interface KldWriter {
    /**
     * 写入文本
     */
    fun write(text: String)

    /**
     * 关闭写入器
     */
    fun close()
}

/**
 * KldWriter 扩展函数
 */

/**
 * 使用完后自动关闭，类似于java.io.Writer.use
 */
inline fun <T> KldWriter.use(block: (KldWriter) -> T): T {
    try {
        return block(this)
    } finally {
        close()
    }
}

/**
 * 写入一行文本
 */
fun KldWriter.writeLine(text: String) {
    write(text)
    write("\n")
}

/**
 * Kaleidoscope 统一元数据解析器
 *
 * 抽象了APT和KSP的核心元数据获取功能，提供统一的API
 */
interface KldResolver {

    /**
     * 获取所有被指定注解标注的元素（通过全限定名称）
     */
    fun getElementsAnnotatedWith(qualifiedName: String): Sequence<KldElement>

    /**
     * 获取所有被指定注解标注的元素（通过简单名称）
     */
    fun getElementsAnnotatedWithSimpleName(simpleName: String): Sequence<KldElement>

    /**
     * 根据全限定名称获取类型元素
     */
    fun getClassDeclaration(qualifiedName: String): KldTypeElement?

    /**
     * 获取包元素
     */
    fun getPackageDeclaration(qualifiedName: String): KldPackageElement?

    /**
     * 获取所有文件
     */
    fun getAllFiles(): Sequence<KldSourceFile>

    /**
     * 获取处理器选项
     */
    fun getOptions(): Map<String, String>

    /**
     * 判断处理是否结束
     */
    val isProcessingOver: Boolean

    /**
     * 获取根元素
     */
    val rootElements: Sequence<KldElement>

    /**
     * 创建源文件 - 返回KldWriter用于写入内容
     */
    fun createSourceFile(
        packageName: String,
        fileName: String,
        vararg originatingElements: KldElement
    ): KldWriter

    /**
     * 记录信息日志
     */
    fun info(message: String, element: KldElement? = null)

    /**
     * 记录警告日志
     */
    fun warn(message: String, element: KldElement? = null)

    /**
     * 记录错误日志
     */
    fun error(message: String, element: KldElement? = null)
}

/**
 * KldResolver 扩展函数
 */

/**
 * 获取指定注解的所有元素并转换为列表
 */
fun KldResolver.getElementsAnnotatedWithList(qualifiedName: String): List<KldElement> {
    return getElementsAnnotatedWith(qualifiedName).toList()
}

/**
 * 检查是否存在被指定注解标注的元素
 */
fun KldResolver.hasElementsAnnotatedWith(qualifiedName: String): Boolean {
    return getElementsAnnotatedWith(qualifiedName).any()
}

/**
 * 获取所有类型元素
 */
fun KldResolver.getAllTypeElements(): Sequence<KldTypeElement> {
    return rootElements.filterIsInstance<KldTypeElement>()
}

/**
 * 根据包名过滤元素
 */
fun KldResolver.getElementsByPackage(packageName: String): Sequence<KldElement> {
    return rootElements.filter { it.packageName == packageName }
}

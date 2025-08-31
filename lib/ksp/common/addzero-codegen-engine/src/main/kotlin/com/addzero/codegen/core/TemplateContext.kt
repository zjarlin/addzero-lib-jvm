package com.addzero.codegen.core

/**
 * 模板上下文接口
 *
 * 用于为 Velocity 模板引擎提供数据上下文
 * 实现此接口来定义模板变量的数据源
 *
 * @param T 元数据类型，表示要处理的数据模型
 */
interface TemplateContext<T> {

    /**
     * 构建模板上下文
     *
     * 将元数据转换为 Velocity 模板所需的变量映射
     * 子类必须实现此方法，手动构建 Map 结构
     *
     * @param metadata 元数据对象
     * @return 模板变量映射，键为模板中的变量名，值为对应的数据
     */
    fun buildContext(metadata: T): Map<String, Any>

    /**
     * 获取输出文件名
     *
     * 根据元数据生成输出文件的名称
     *
     * @param metadata 元数据对象
     * @return 输出文件名（包含扩展名）
     */
    fun getOutputFileName(metadata: T): String

    /**
     * 获取输出包名
     *
     * 根据元数据确定生成代码的包名
     *
     * @param metadata 元数据对象
     * @return 输出包名
     */
    fun getOutputPackage(metadata: T): String

    /**
     * 获取模板名称
     *
     * 返回用于代码生成的模板文件名
     *
     * @param metadata 元数据对象
     * @return 模板文件名
     */
    fun getTemplateName(metadata: T): String

    /**
     * 是否应该覆盖已存在的文件
     *
     * 针对每个模板单独控制覆盖策略
     *
     * @param metadata 元数据对象
     * @return true 表示覆盖已存在文件，false 表示跳过已存在文件
     */
    fun shouldOverwriteExisting(metadata: T): Boolean = false

    /**
     * 是否应该跳过生成
     *
     * 根据元数据判断是否应该跳过该项的代码生成
     *
     * @param metadata 元数据对象
     * @return true 表示跳过生成，false 表示继续生成
     */
    fun shouldSkip(metadata: T): Boolean = false

    /**
     * 获取生成前的回调
     *
     * 在代码生成前执行的自定义逻辑
     *
     * @param metadata 元数据对象
     */
    fun beforeGenerate(metadata: T) {}

    /**
     * 获取生成后的回调
     *
     * 在代码生成后执行的自定义逻辑
     *
     * @param metadata 元数据对象
     * @param generatedCode 生成的代码内容
     */
    fun afterGenerate(metadata: T, generatedCode: String) {}
}

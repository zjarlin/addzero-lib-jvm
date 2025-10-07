package site.addzero.app

import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/**
 * 泛型REPL接口，支持复杂参数解析
 * @param P 参数容器类型(数据类)
 * @param O 输出类型
 */
interface AdvancedRepl<P, O> {
    /** 命令标识符 */
    val command: String

    /** 命令描述 */
    val description: String

    /** 参数定义列表 */
    val paramDefs: List<ParamDef>

    /** 执行核心逻辑 */
    fun eval(params: P): O

    /** 格式化输出结果 */
    fun print(output: O): String = output.toString()

    /** 异常处理 */
    fun handleError(e: Exception): String = "错误: ${e.message}"

    /**
     * 创建参数对象实例
     * @param values 参数值列表，按paramDefs顺序排列
     */
    fun createParams(values: List<Any?>): P

    /**
     * 解析输入为参数容器
     * 自动处理类型转换(y/n->Boolean)、默认值填充
     */
    fun parseParams(input: List<String>): P {
        val paramValues = mutableListOf<Any?>()

        // 按定义解析每个参数
        paramDefs.forEachIndexed { index, def ->
            val inputValue = input.getOrNull(index)

            // 如果输入为空字符串或者null，并且有默认值，则使用默认值
            val valueToUse = if (inputValue.isNullOrEmpty() && def.defaultValue != null) {
                def.defaultValue
            } else if (inputValue.isNullOrEmpty() && def.isRequired) {
                throw IllegalArgumentException("缺少必填参数: ${def.name}")
            } else if (inputValue.isNullOrEmpty()) {
                def.defaultValue
            } else {
                inputValue
            }

            // 如果最终值为null且为必填参数，则抛出异常
            if (valueToUse == null && def.isRequired) {
                throw IllegalArgumentException("缺少必填参数: ${def.name}")
            }

            // 类型转换
            val parsedValue = when {
                def.type.isSubtypeOf(typeOf<Boolean>()) -> {
                    when (valueToUse.toString().lowercase()) {
                        "y", "yes", "true" -> true
                        "n", "no", "false" -> false
                        else -> valueToUse as? Boolean ?: throw IllegalArgumentException("布尔参数${def.name}需输入y/n")
                    }
                }

                def.type.isSubtypeOf(typeOf<Int>()) -> valueToUse?.toString()?.toIntOrNull()
                def.type.isSubtypeOf(typeOf<Double>()) -> valueToUse?.toString()?.toDoubleOrNull()
                def.type.isSubtypeOf(typeOf<String>()) -> valueToUse?.toString()
                else -> valueToUse // 支持自定义类型扩展
            }

            paramValues.add(parsedValue)
        }

        return createParams(paramValues)
    }

    /** 生成参数帮助信息 */
    fun getParamHelp(): String {
        return paramDefs.joinToString("\n  ") { def ->
            val requiredMark = if (def.isRequired) "*" else ""
            val defaultHint = def.defaultValue?.let { " (默认: $it)" } ?: ""
            "${def.name}$requiredMark: ${def.description}${defaultHint} (类型: ${def.type})"
        }
    }
}

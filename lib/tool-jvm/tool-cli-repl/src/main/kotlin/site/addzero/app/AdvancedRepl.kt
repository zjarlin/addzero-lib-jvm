package site.addzero.app

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * 泛型REPL接口，支持复杂参数解析
 * @param P 参数容器类型(数据类)
 * @param O 输出类型
 */
interface AdvancedRepl<P : Any, O> {
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

    /** 检查当前REPL是否受支持(默认为true) */
    fun support(): Boolean = true

    /**
     * 参数容器的类型信息，通过反射自动获取泛型类型
     */
    @Suppress("UNCHECKED_CAST")
    val paramClass: KClass<out P>
        get() {
            val genericSuperclass = this::class.supertypes
                .find { it.classifier == AdvancedRepl::class }
                ?: throw IllegalStateException("无法找到 AdvancedRepl 父类型")

            val paramType = genericSuperclass.arguments.first().type?.jvmErasure
                ?: throw IllegalStateException("无法获取参数类型 P")

            return paramType as KClass<out P>
        }


    /**
     * 创建参数对象实例
     * 使用反射自动创建，子类可以重写来自定义逻辑
     * @param values 参数值列表，按paramDefs顺序排列
     */
    fun createParams(values: List<Any?>): P {
        val clazz = paramClass

        // 特殊处理 Unit 类型
        if (clazz == Unit::class) {
            @Suppress("UNCHECKED_CAST")
            return Unit as P
        }

        // 确保传入的参数列表不为空（对于非Unit类型）
        if (values.isEmpty()) {
            throw IllegalArgumentException("参数类型 ${clazz.simpleName} 需要参数，但传入了空列表")
        }

        @Suppress("UNCHECKED_CAST")
        val anyClass = clazz as KClass<Any>
        val constructor = anyClass.primaryConstructor
            ?: throw IllegalArgumentException("${clazz.simpleName} 必须有主构造函数")

        val parameters = constructor.valueParameters
        if (parameters.size != values.size) {
            throw IllegalArgumentException(
                "参数数量不匹配: 构造函数需要 ${parameters.size} 个参数，但提供了 ${values.size} 个"
            )
        }

        // 按位置映射参数值
        val args = parameters.mapIndexed { index, param ->
            val value = values[index]
            // 使用工具类进行类型转换
            ParamUtils.convertValueIfNeeded(value, param.type.classifier)
        }

        @Suppress("UNCHECKED_CAST")
        return constructor.call(*args.toTypedArray()) as P
    }

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


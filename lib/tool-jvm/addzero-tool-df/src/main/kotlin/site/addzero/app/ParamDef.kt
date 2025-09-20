package site.addzero.app

import site.addzero.cli.setting.SettingContext.EXIT_COMMAND
import site.addzero.cli.setting.SettingContext.HELP_COMMAND
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/**
 * 参数定义类，描述单个参数的元信息
 * @param name 参数名称
 * @param type 参数类型(KType)
 * @param description 参数描述
 * @param defaultValue 默认值(可选)
 * @param isRequired 是否必填
 */
data class ParamDef(
    val name: String,
    val type: KType,
    val description: String,
    val defaultValue: Any? = null,
    val isRequired: Boolean = defaultValue == null
)

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


/**
 * 扩展函数：将Repl列表转换为可运行的REPL循环
 */
fun <P, O> List<AdvancedRepl<P, O>>.toAdvancedRepl(
    prompt: String = "> ",
    exitCommand: String = EXIT_COMMAND,
    helpCommand: String = HELP_COMMAND
) {
    val commandMap = associateBy { it.command.lowercase() }
    val indexedCommands = this.withIndex().associate { (index, repl) -> (index + 1) to repl }

    printWelcomeAndCommands(indexedCommands)

    while (true) {
        print(prompt)
        val inputLine = readLine()?.trim() ?: continue
        val (cmd, args) = splitCommandAndArgs(inputLine)

        when {
            cmd.equals(exitCommand, ignoreCase = true) -> {
                println("退出程序")
                return
            }

            cmd.equals(helpCommand, ignoreCase = true) -> {
                printHelp(commandMap)
                printCommandList(indexedCommands)
            }

            cmd.matches(Regex("^\\d+$")) -> {
                val cmdIndex = cmd.toIntOrNull()
                if (cmdIndex != null && indexedCommands.containsKey(cmdIndex)) {
                    executeCommandByIndex(cmdIndex, indexedCommands)
                } else {
                    println("无效的命令编号，请输入有效的数字")
                    printCommandList(indexedCommands)
                }
            }

            else -> executeCommand(cmd, args, commandMap, indexedCommands)
        }
    }
}

// 打印欢迎信息和命令列表
private fun <P, O> printWelcomeAndCommands(indexedCommands: Map<Int, AdvancedRepl<P, O>>) {
    println("REPL启动，输入命令编号执行命令，输入'${HELP_COMMAND}'查看命令详情，'${EXIT_COMMAND}'退出")
    printCommandList(indexedCommands)
}

// 打印命令列表
private fun <P, O> printCommandList(indexedCommands: Map<Int, AdvancedRepl<P, O>>) {
    println("\n可用命令(键入数字和短名称都可以执行命令):")
    indexedCommands.forEach { (index, repl) ->
        println("  $index. ${repl.command} - ${repl.description}")
    }
    println()
}

// 根据索引执行命令
private fun <P, O> executeCommandByIndex(
    cmdIndex: Int,
    indexedCommands: Map<Int, AdvancedRepl<P, O>>
) {
    val repl = indexedCommands[cmdIndex]!!

    // 有参数需要用户输入（即使是可选参数也给用户修改的机会）
    val paramValues = mutableListOf<String>()
    repl.paramDefs.forEach { paramDef ->
        printParamPrompt<Any, Any>(paramDef, paramValues)
        val input = readLine()?.trim() ?: ""
        // 如果用户直接按回车且有默认值，则使用默认值
        val valueToUse = if (input.isEmpty() && paramDef.defaultValue != null) {
            paramDef.defaultValue.toString()
        } else {
            input
        }
        paramValues.add(valueToUse)
    }

    try {
        // 由于我们不能通过反射正确创建参数对象，我们直接调用eval方法使用默认参数
        // 这里我们假设子类会正确处理参数
        val params = repl.parseParams(paramValues)
        val result = repl.eval(params)
        println(repl.print(result))
    } catch (e: Exception) {
        System.err.println(repl.handleError(e))
    }
    // 执行完命令后再次打印可用命令列表
    printCommandList(indexedCommands)
}

// 打印参数提示信息
private fun <P, O> printParamPrompt(paramDef: ParamDef, currentValues: List<String>) {
    val requiredMark = if (paramDef.isRequired) "*" else ""
    val defaultHint = paramDef.defaultValue?.let { " (默认: $it)" } ?: ""
    val exampleValue = getExampleValue(paramDef)
    val typeName = paramDef.type.toString().substringAfterLast(".")

    println("${paramDef.name}$requiredMark: ${paramDef.description}${defaultHint} (类型: $typeName$exampleValue)")
    if (paramDef.defaultValue != null) {
        print("请输入${paramDef.name}的值$defaultHint: ")
    } else {
        print("请输入${paramDef.name}的值: ")
    }
}

// 获取示例值
private fun getExampleValue(paramDef: ParamDef): String {
    return when {
        paramDef.type.isSubtypeOf(typeOf<Boolean>()) -> ", 示例: true/false 或 y/n"
        paramDef.type.isSubtypeOf(typeOf<Int>()) -> ", 示例: 42"
        paramDef.type.isSubtypeOf(typeOf<Double>()) -> ", 示例: 3.14"
        paramDef.type.isSubtypeOf(typeOf<String>()) -> ", 示例: \"文本\""
        else -> ""
    }
}

// 分割命令和参数
private fun splitCommandAndArgs(input: String): Pair<String, List<String>> {
    val parts = input.split(Regex("\\s+"), limit = 2)
    return when (parts.size) {
        1 -> parts[0] to emptyList()
        else -> parts[0] to parts[1].split(Regex("\\s+"))
    }
}

// 打印帮助信息
private fun <P, O> printHelp(commandMap: Map<String, AdvancedRepl<P, O>>) {
    println("可用命令(键入数字和短名称都可以执行命令):")
    commandMap.values.forEach { repl ->
        println("\n${repl.command}: ${repl.description}")
        println("  参数:")
        println("  ${repl.getParamHelp()}")
    }
}

// 执行命令
private fun <P, O> executeCommand(
    cmd: String,
    args: List<String>,
    commandMap: Map<String, AdvancedRepl<P, O>>,
    indexedCommands: Map<Int, AdvancedRepl<P, O>>
) {
    val repl = commandMap[cmd.lowercase()] ?: run {
        println("未知命令: $cmd，输入'help'查看帮助")
        printCommandList(indexedCommands)
        return
    }

    try {
        val params = repl.parseParams(args)
        val result = repl.eval(params)
        println(repl.print(result))
    } catch (e: Exception) {
        System.err.println(repl.handleError(e))
        System.err.println("参数格式错误，正确用法: ${repl.command} ${repl.paramDefs.joinToString(" ") { "<${it.name}>" }}")
    }

    // 执行完命令后再次打印可用命令列表
    printCommandList(indexedCommands)
}

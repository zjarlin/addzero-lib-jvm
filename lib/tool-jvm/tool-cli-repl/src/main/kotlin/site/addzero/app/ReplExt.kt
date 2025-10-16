package site.addzero.app

import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

    const val EXIT_COMMAND = "q"

    const val HELP_COMMAND = "h"

// 打印欢迎信息和命令列表
private fun <P : Any, O> printWelcomeAndCommands(indexedCommands: Map<Int, AdvancedRepl<P, O>>) {
    println("REPL启动，输入命令编号执行命令，输入'${HELP_COMMAND}'查看命令详情，'${EXIT_COMMAND}'退出")
    printCommandList(indexedCommands)
}

// 打印命令列表
private fun <P : Any, O> printCommandList(indexedCommands: Map<Int, AdvancedRepl<P, O>>) {
    println("\n可用命令(键入数字和短名称都可以执行命令):")
    indexedCommands.forEach { (index, repl) ->
        println("  $index. ${repl.command} - ${repl.description}")
    }
    println()
}

// 根据索引执行命令
private fun <P : Any, O> executeCommandByIndex(
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
private fun <P : Any, O> printHelp(commandMap: Map<String, AdvancedRepl<P, O>>) {
    println("可用命令(键入数字和短名称都可以执行命令):")
    commandMap.values.forEach { repl ->
        println("\n${repl.command}: ${repl.description}")
        println("  参数:")
        println("  ${repl.getParamHelp()}")
    }
}

// 执行命令
private fun <P : Any, O> executeCommand(
    cmd: String,
    args: List<String>,
    commandMap: Map<String, AdvancedRepl<P, O>>,
    indexedCommands: Map<Int, AdvancedRepl<P, O>>
) {
    val repl = commandMap[cmd.lowercase()] ?: run {
        println("未知命令: $cmd，输入${HELP_COMMAND}查看帮助")
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

/**
 * 扩展函数：将Repl列表转换为可运行的REPL循环
 */
fun <P : Any, O> List<AdvancedRepl<P, O>>.toAdvancedRepl(
    prompt: String = "> ",
    exitCommand: String = EXIT_COMMAND,
    helpCommand: String = HELP_COMMAND
) {
    // 过滤出支持的REPL命令
    val supportedCommands = this.filter { it.support() }
    val unsupportedCount = this.size - supportedCommands.size

    if (unsupportedCount > 0) {
        println("注意: 有 $unsupportedCount 个命令在当前环境下不受支持")
    }

    val commandMap = supportedCommands.associateBy { it.command.lowercase() }
    val indexedCommands = supportedCommands.withIndex().associate { (index, repl) -> (index + 1) to repl }

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

fun List<AdvancedRepl<*, *>>.startReplMode() {
    @Suppress("UNCHECKED_CAST")
    (this as List<AdvancedRepl<Any, Any>>).toAdvancedRepl(
        prompt = "dotfiles-cli > ",
        exitCommand = EXIT_COMMAND,
        helpCommand = HELP_COMMAND
    )
}

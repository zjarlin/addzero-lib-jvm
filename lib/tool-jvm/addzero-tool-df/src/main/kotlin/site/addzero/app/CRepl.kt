@file:OptIn(ExperimentalCli::class)

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import java.util.*

//inline fun <reified T: Subcommand>T.getOptions(): Unit {
//
//
//
//
//    val klass = T::class
//    val declaredFunctions = klass.declaredFunctions
//    val declaredMemberProperties = klass.memberProperties.map {
//       optionFullFormPrefix
//    }
//    println()
//
//}

// 反射获取Subcommand的参数信息
fun getSubcommandArguments(subcommand: Subcommand): List<ArgumentInfo> {
//    val argumentInfos = mutableListOf<ArgumentInfo>()
    // 获取Subcommand类中所有声明的成员函数
//    val prop = subcommand::class.memberProperties
//    prop.filter {
//
//       it.name in listOf("", "actionDescription")
//    }.getFirst()
    return emptyList()
}

data class ArgumentInfo(
    val name: String,
    val type: String,
    val description: String
)

@OptIn(ExperimentalCli::class)
class NumberSelectableCliRepl(
    private val programName: String,
    private val builtInCommands: List<Subcommand> = emptyList()
) {
    // 存储自定义命令的列表
    private val customCommands = mutableListOf<Subcommand>()

    // 合并内置命令和自定义命令
    private val allCommands: List<Subcommand>
        get() = builtInCommands + customCommands

    // 初始化CLI解析器
    private val cliParser = ArgParser(programName).apply {
        subcommands(*allCommands.toTypedArray())
    }

    // 添加单个自定义命令
    fun addCustomCommand(command: Subcommand) {
        customCommands.add(command)
        cliParser.subcommands(*allCommands.toTypedArray())
    }

    // 批量添加自定义命令
    fun addCustomCommands(commands: List<Subcommand>) {
        customCommands.addAll(commands)
        cliParser.subcommands(*allCommands.toTypedArray())

    }

    private fun handleNumberSelection(selection: Int) {
        // 检查数字是否在有效范围内
        if (selection < 1 || selection > allCommands.size) {
            System.err.println("无效的数字选择，请输入1到${allCommands.size}之间的数字")
            return
        }

        // 获取选中的命令
        val selectedCommand = allCommands[selection - 1]
        println("你选择了: ${selectedCommand.name} - ${selectedCommand.actionDescription}")
//        val subcommandArguments = getSubcommandArguments(selectedCommand)
//        selectedCommand.getOptions()

        // 提示用户输入命令参数
        print("请输入命令参数（直接回车表示无参数）: ")
        val argsInput = Scanner(System.`in`).nextLine().trim()

        // 构建完整的命令参数数组
        val fullArgs = mutableListOf(selectedCommand.name)
//        if (argsInput.isNotEmpty()) {
        fullArgs.addAll(splitCommandLine(argsInput))
//        }
        val helpMessage = selectedCommand.helpMessage
        val option = selectedCommand.option(ArgType.String)
        val delegate = option.delegate
        val valueOrigin = option.valueOrigin
        val bool = option.value == null
        val hasAugment = option != null
//        if (hasAugment) {
//            //有参数的情况
//            //添加一个占位保证cli不报错
////            fullArgs.add("")
//        }
        // 执行命令
        try {
            val args = fullArgs.toTypedArray()
            cliParser.parse(args)
        } catch (e: Exception) {
            e.printStackTrace()
//            System.err.println("执行命令时出错: ${e.message ?: "未知错误"}")
            return

        }
    }

    // 启动REPL循环
    fun start() {
        val scanner = Scanner(System.`in`)
        println(
            """=== $programName 交互式命令行 ===
            |输入数字选择命令，或直接输入命令名称及参数
            |输入 'l' 重新列出命令，'q' 退出
            |""".trimMargin()
        )

        // 初始列出所有命令
        listCommands()

        while (true) {
            print("$programName > ")
            val input = scanner.nextLine().trim()

            when {
                input.equals("q", ignoreCase = true) -> {
                    println("再见！")
                    return
                }

                input.equals("l", ignoreCase = true) -> {
                    listCommands()
                    continue
                }


                input.equals("h", ignoreCase = true) -> {
                    printHelp()
                    return
                }

                input.isEmpty() -> continue
                // 处理数字选择
                input.matches(Regex("^\\d+$")) -> {
                    handleNumberSelection(input.toInt())
                }
                // 处理直接命令输入
                else -> {
                    try {
                        val args = splitCommandLine(input)
                        cliParser.parse(args)
                    } catch (e: Exception) {
                        System.err.println("错误: ${e.message ?: "未知错误"}")
                    }
                }
            }
        }
    }

    private fun printHelp() {
        val joinToString = allCommands.joinToString(System.lineSeparator()) {
//            it.subcommands()
            it.helpMessage
        }
        println(joinToString)
    }

    // 列出所有可用命令及其数字编号
    private fun listCommands() {
        println("\n可用命令列表：")
        allCommands.forEachIndexed { index, command ->
            println("  ${index + 1}. ${command.name} - ${command.actionDescription}")
        }
        println("输入数字选择命令，或直接输入命令名称及参数\n")
    }

    // 处理数字选择命令

    // 命令行解析（支持引号和转义字符）
    private fun splitCommandLine(input: String): Array<String> {
        val regex = Regex("""("([^"\\]|\\.)*"|'([^'\\]|\\.)*'|\S+)""")
        return regex.findAll(input)
            .map { match ->
                val token = match.value
                // 移除引号并处理转义字符
                if ((token.startsWith('"') && token.endsWith('"')) ||
                    (token.startsWith('\'') && token.endsWith('\''))
                ) {
                    token.substring(1, token.length - 1)
                        .replace(Regex("\\\\(.)"), "$1")
                } else {
                    token.replace(Regex("\\\\(.)"), "$1")
                }
            }
            .toList()
            .toTypedArray()
    }
}

package site.addzero.cli.platform

import java.io.File

interface PlatformStrategy {
    val support: Boolean

    /**
     * 读取用户输入的一行文本
     * @return 用户输入的文本，如果发生错误则返回null
     */
    fun readLine(): String? {
        return kotlin.io.readLine()?.trim()
    }

    fun readLine(prompt: String): String? {
        println(prompt)
        return readLine()
    }


    fun readYesNo(prompt: String): Boolean? {
        while (true) {
            println("$prompt  (y/n)，默认y:")
            val input = readLine("$prompt  (y/n)，默认y:")?.lowercase()
            when (input) {
                "y", "yes", "", null -> return true
                "n", "no" -> return false
                else -> println("Invalid input. Please enter 'y' or 'n'.")
            }
        }
    }


    /**
     * 获取可用的处理器数量
     * @return 处理器数量
     */
    fun getAvailableProcessors(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    /**
     * 退出进程
     * @param status 退出状态码
     */
    fun exitProcess(status: Int): Nothing {
        return kotlin.system.exitProcess(status)
    }


    /**
     * 创建目录
     * @param path 目录路径
     * @return 是否创建成功
     */
    fun mkdir(path: String): Boolean {
        return try {
            File(path).mkdirs()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容
     */
    fun readFile(path: String): String {
        return File(path).readText()
    }

    /**
     * 写入文件内容
     * @param path 文件路径
     * @param content 文件内容
     * @return 是否写入成功
     */
    fun writeFile(path: String, content: String): Boolean {
        return try {
            File(path).writeText(content)
            true
        } catch (e: Exception) {
            false
        }

    }

    /**
     * 获取环境变量
     * @param name 环境变量名称
     * @return 环境变量值，如果不存在则返回null
     */
    fun getEnv(name: String): String? {
        return System.getenv(name)
    }

    /**
     * 设置环境变量
     * @param name 环境变量名称
     * @param value 环境变量值
     */
    fun setEnv(name: String, value: String)

    /**
     * 获取用户主目录
     * @return 用户主目录路径
     */
    fun getHomeDir(): String {
        return System.getProperty("user.home")
    }

    /**
     * 判断路径是否为目录
     * @param path 路径
     * @return 是否为目录
     */
    fun isDirectory(path: String): Boolean {
        return File(path).isDirectory
    }

    /**
     * 执行命令
     * @param command 命令
     * @param timeout 超时时间（毫秒）
     * @return Pair<退出码, 输出内容>
     */
    fun executeCommand(command: String, timeout: Long = 99999): CommandResult

    /**
     * 获取当前平台类型
     * @return 平台类型
     */
    fun getPlatformType(): PlatformType


}

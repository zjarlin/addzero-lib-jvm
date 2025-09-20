package site.addzero.cli.platform

import org.koin.core.annotation.Single
import site.addzero.cli.platform.ProUtil.handleProcessOutput
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Windows平台工具实现
 */
@Single
class WindowsPlatformStrategy : PlatformStrategy {
    override val support: Boolean
        get() = run {
            val osName = System.getProperty("os.name").lowercase()
            osName.contains("win")
        }

    override fun setEnv(name: String, value: String) {
        // setx命令格式：setx 变量名 "变量值"
        val process = ProcessBuilder("cmd.exe", "/c", "setx", name, value)
            .redirectErrorStream(true)
            .start()

        handleProcessOutput(process)
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            System.err.println("Windows设置环境变量失败，错误码: " + exitCode)
        }
        println("Windows环境变量设置成功（需重启终端生效）: " + name + "=" + value)
    }


    override fun executeCommand(command: String, timeout: Long): CommandResult {
        return try {
            val process = ProcessBuilder("cmd", "/c", command).start()
            val finished = process.waitFor(timeout, TimeUnit.MILLISECONDS)

            if (finished) {
                val exitCode = process.exitValue()
                val output = process.inputStream.bufferedReader().use { it.readText() }
                CommandResult(exitCode, output, "")

            } else {
                process.destroyForcibly()
                CommandResult(-1, "Command timed out", "")
            }
        } catch (e: Exception) {
            CommandResult(-1, "Error executing command: ${e.message}", "Error executing command: ${e.message}")
        }
    }

    override fun getPlatformType(): PlatformType {
        return PlatformType.WINDOWS
    }

    override fun createSymlink(absolutePath: String, linkPath: String): Boolean {
        val target = File(absolutePath)
        val link = File(linkPath)

        val isDirectory = target.isDirectory
        val command = if (isDirectory) {
            "mklink /D \"${link.absolutePath}\" \"${target.absolutePath}\""
        } else {
            "mklink \"${link.absolutePath}\" \"${target.absolutePath}\""
        }

        return try {
            val process = ProcessBuilder("cmd.exe", "/c", command)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()

            if (exitCode == 0) {
                true
            } else {
                System.err.println("Windows创建失败 (错误码: $exitCode): $output")
                false
            }
        } catch (e: Exception) {
            System.err.println("Windows创建异常: ${e.message}")
            false
        }
    }
}

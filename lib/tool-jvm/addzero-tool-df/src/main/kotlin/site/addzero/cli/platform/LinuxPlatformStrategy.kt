package site.addzero.cli.platform

import org.koin.core.annotation.Single
import site.addzero.cli.platform.ProUtil.setUnixEnv
import java.util.concurrent.TimeUnit

/**
 * Linux平台工具实现
 */
@Single
class LinuxPlatformStrategy : PlatformStrategy {
    override val support: Boolean
        get() = run {
            val osName = System.getProperty("os.name").lowercase()
            osName.contains("nux")
        }

    override fun setEnv(name: String, value: String) {

        // 优先检测是否使用zsh（macOS默认shell）
        var shellConfig = System.getProperty("user.home") + "/.zshrc"
        val checkZsh = ProcessBuilder("ls", shellConfig).start()
        if (checkZsh.waitFor() != 0) {
            // 不存在.zshrc则使用.bash_profile
            shellConfig = System.getProperty("user.home") + "/.bash_profile"
        }
        setUnixEnv(name, value, shellConfig)
    }


    override fun executeCommand(command: String, timeout: Long): CommandResult {
        return try {
            val process = ProcessBuilder("bash", "-c", command).start()
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
        return PlatformType.LINUX
    }

}

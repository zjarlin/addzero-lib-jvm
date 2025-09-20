package site.addzero.cli.platform

import org.koin.core.annotation.Single
import site.addzero.cli.platform.ProUtil.setUnixEnv


/**
 * MacOS平台工具实现
 */
@Single
class MacOSPlatformStrategy : PlatformStrategy {

    override val support: Boolean
        get() = run {
            val osName = System.getProperty("os.name").lowercase()
            osName.contains("mac")
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
        return LinuxPlatformStrategy().executeCommand(command, timeout)
    }

    override fun getPlatformType(): PlatformType {
        return PlatformType.MACOS
    }

}

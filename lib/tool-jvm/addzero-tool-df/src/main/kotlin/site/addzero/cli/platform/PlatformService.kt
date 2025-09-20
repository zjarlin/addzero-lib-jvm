package site.addzero.cli.platform

import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject

@Single
class Temp(val strategys: List<PlatformStrategy>)

val temp: Temp by inject(Temp::class.java)
val act = run {
    temp.strategys.firstOrNull { it.support } ?: LinuxPlatformStrategy()
}

/**
 * 平台工具类提供跨平台操作
 */
object PlatformService : PlatformStrategy by act {

    fun isWindows(): Boolean {
        return act.getPlatformType() == PlatformType.WINDOWS
    }


    fun isMac(): Boolean {
        return act.getPlatformType() == PlatformType.MACOS
    }

    fun isLinux(): Boolean {
        return act.getPlatformType() == PlatformType.LINUX
    }
}


fun String.runCmd(): CommandResult {
    return PlatformService.executeCommand(this)
}

fun String.runBoolean(): Boolean {
    return runCmd().exitCode == 0
}

fun String.runCode(): Int {
    return runCmd().exitCode
}

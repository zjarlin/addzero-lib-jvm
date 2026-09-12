package site.addzero.starter

import io.ktor.server.application.Application

/**
 * 应用启动器 SPI 接口。
 *
 * 实现由宿主的发现机制注册，并按 [order] 从小到大执行。
 */
interface AppStarter {
    /** 排序值，越小越先执行。 */
    val order get() = Int.MAX_VALUE

    /** 返回 false 时跳过当前启动器。 */
    fun Application.enable() = true

    /** 安装当前启动器。 */
    fun Application.onInstall()
}

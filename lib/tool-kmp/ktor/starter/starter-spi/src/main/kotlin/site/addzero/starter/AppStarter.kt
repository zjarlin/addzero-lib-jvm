package site.addzero.starter

import io.ktor.server.application.*

/**
 * 应用启动器 SPI 接口。
 *
 * 任何模块只要实现此接口并被发现机制注册（当前使用 Koin），
 * 就会在主应用启动时被自动调用。
 *
 * 设计为框架无关：接口本身不绑定 Koin 或 ServiceLoader，
 * 未来可无缝切换到 KMP 标准 SPI。
 */
interface AppStarter {
    /** 排序值，越小越先执行 */
    val order get() = Int.MAX_VALUE

    /** 条件判断，返回 false 则跳过此 Starter */
    fun Application.enable() = true

    /** 执行安装逻辑 */
    fun Application.onInstall()
}

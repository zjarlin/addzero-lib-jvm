package site.addzero.kcloud.jimmer.plugin

import io.ktor.server.application.*

/**
 * Jimmer Ktor 插件。
 *
 * 通过 Koin 自动管理 KSqlClient 的生命周期。
 * 核心装配逻辑定义在 site.addzero.kcloud.jimmer.di 包中。
 */
val JimmerPlugin = createApplicationPlugin(name = "JimmerPlugin") {
    // 预留，目前主要依赖 Koin 自动扫描和注册。
}

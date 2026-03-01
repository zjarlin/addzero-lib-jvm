package site.addzero.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Plugin: 自动扫描项目中预编译脚本所需的 catalog alias，
 * 检查消费方的 gradle/libs.versions.toml 是否已声明，
 * 对缺失的 alias 自动生成 TOML 条目并追加写入。
 *
 * 用法：在消费方 settings.gradle.kts 或 build.gradle.kts 中 apply：
 * ```
 * plugins {
 *     id("site.addzero.gradle.plugin.catalog-autoresolve")
 * }
 * ```
 */
class CatalogAutoresolvePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("catalogAutoresolve", CatalogAutoresolveTask::class.java) {
            group = "catalog"
            description = "Scan convention plugin aliases and auto-add missing entries to libs.versions.toml"
        }
    }
}

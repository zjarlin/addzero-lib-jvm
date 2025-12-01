package site.addzero.gradle.plugin

import site.addzero.network.call.maven.util.MavenCentralSearchUtil
import site.addzero.util.VersionUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

fun String?.isValid() = !isNullOrBlank() && this != "unspecified"

// 版本缓存
val versionCache = ConcurrentHashMap<String, String>()

// 异步获取模块版本
fun fetchVersionAsync(groupId: String, artifactId: String): CompletableFuture<String?> =
    CompletableFuture.supplyAsync {
        runCatching { MavenCentralSearchUtil.getLatestVersion(groupId, artifactId) }
            .getOrNull()
    }

// 延迟到配置完成后执行，确保 group/version 已设置
afterEvaluate {
    val groupId = project.group.toString().takeIf { it.isValid() }
        ?: error("[VersionBuddy] group is required")

    val propertyVersion = findProperty("version")?.toString()?.takeIf { it.isValid() } ?: ""
    val rootProjectVersion = project.version.toString().takeIf { it.isValid() } ?: ""

    // 计算模块的最终版本
    fun resolveModuleVersion(mavenVersion: String?): String = when {
        propertyVersion.isNotBlank() -> propertyVersion
        rootProjectVersion.isNotBlank() -> rootProjectVersion
        mavenVersion.isNullOrBlank() -> VersionUtils.defaultVersion()
        else -> VersionUtils.nextVersion(mavenVersion)
    }

    fun versionSource(mavenVersion: String?) = when {
        propertyVersion.isNotBlank() -> "property"
        rootProjectVersion.isNotBlank() -> "rootProject"
        mavenVersion.isNullOrBlank() -> "default"
        else -> "maven($mavenVersion)+1"
    }

    val targetProjects = subprojects.filter { it.path.startsWith(":lib:") }

    if (targetProjects.isEmpty()) {
        println("[VersionBuddy] No :lib: subprojects found")
        return@afterEvaluate
    }

    // 并行异步查询所有模块的 Maven 版本
    val versionFutures = targetProjects.associate { it.name to fetchVersionAsync(groupId, it.name) }

    // 等待所有异步查询完成
    val mavenVersions = versionFutures.mapValues { (_, future) ->
        runCatching { future.get() }.getOrNull()
    }

    // 应用版本到各子项目
    targetProjects.forEach { subProject ->
        val mavenVersion = mavenVersions[subProject.name]
        val finalVersion = resolveModuleVersion(mavenVersion)

        versionCache[subProject.name] = finalVersion
        subProject.version = finalVersion

        println("[VersionBuddy] ${subProject.path} => $finalVersion [${versionSource(mavenVersion)}]")
    }
}

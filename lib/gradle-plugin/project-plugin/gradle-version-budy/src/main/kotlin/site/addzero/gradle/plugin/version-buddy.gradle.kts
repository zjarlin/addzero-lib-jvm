package site.addzero.gradle.plugin

import site.addzero.network.call.maven.util.MavenCentralSearchUtil
import site.addzero.util.VersionUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

fun firstNotBlank(vararg values: String?): String? =
    values.firstOrNull { !it.isNullOrBlank() }

val groupId = firstNotBlank(project.group.toString().takeIf { it.isNotBlank() && it != "unspecified" }, "")
    ?: error("auto version error, you must set group")

val propertyVersion = findProperty("version")?.toString()?.takeIf { it != "unspecified" } ?: ""
val rootProjectVersion = project.version.toString().takeIf { it.isNotBlank() && it != "unspecified" } ?: ""

// 版本缓存，避免重复查询
val versionCache = ConcurrentHashMap<String, String>()

// 异步获取模块版本
fun fetchVersionAsync(groupId: String, artifactId: String): CompletableFuture<String?> =
    CompletableFuture.supplyAsync {
        runCatching { MavenCentralSearchUtil.getLatestVersion(groupId, artifactId) }
            .getOrNull()
    }

// 计算模块的最终版本
fun resolveModuleVersion(groupId: String, artifactId: String, mavenVersion: String?): String {
    return when {
        propertyVersion.isNotBlank() -> propertyVersion
        rootProjectVersion.isNotBlank() -> rootProjectVersion
        mavenVersion.isNullOrBlank() -> VersionUtils.defaultVersion()
        else -> VersionUtils.nextVersion(mavenVersion)
    }
}

// 收集需要查询版本的子项目
val targetProjects = subprojects.filter { it.path.startsWith(":lib:") }

if (targetProjects.isEmpty()) {
    println("[VersionBuddy] No :lib: subprojects found")
} else {
    // 并行异步查询所有模块的 Maven 版本
    val versionFutures = targetProjects.associate { subProject ->
        val artifactId = subProject.name
        artifactId to fetchVersionAsync(groupId, artifactId)
    }

    // 等待所有异步查询完成并收集结果
    val mavenVersions = versionFutures.mapValues { (_, future) ->
        runCatching { future.get() }.getOrNull()
    }

    // 应用版本到各子项目
    targetProjects.forEach { subProject ->
        val artifactId = subProject.name
        val mavenVersion = mavenVersions[artifactId]
        val finalVersion = resolveModuleVersion(groupId, artifactId, mavenVersion)

        versionCache[artifactId] = finalVersion
        subProject.version = finalVersion

        val versionSource = when {
            propertyVersion.isNotBlank() -> "property"
            rootProjectVersion.isNotBlank() -> "rootProject"
            mavenVersion.isNullOrBlank() -> "default"
            else -> "maven($mavenVersion)+1"
        }
        println("[VersionBuddy] ${subProject.path} => $finalVersion [$versionSource]")
    }
}

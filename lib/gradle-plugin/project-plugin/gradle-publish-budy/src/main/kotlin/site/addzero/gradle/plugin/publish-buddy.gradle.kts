package site.addzero.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import site.addzero.gradle.PublishConventionExtension
import site.addzero.util.createExtension
import java.time.LocalDate
import java.util.ArrayDeque

// 默认配置常量
object Defaults {
    const val AUTHOR_NAME = "zjarlin"
    const val GIT_URL = "https://github.com/zjarlin/addzero-lib-jvm.git"
    const val EMAIL_DOMAIN = "outlook.com"
    const val LICENSE_NAME = "The Apache License, Version 2.0"
    const val LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
}

fun Project.readmeSummaryOrNull(): String? {
    val candidates = listOf("README.md", "README.MD", "readme.md", "README")
    val readme = candidates.map { file(it) }.firstOrNull { it.exists() && it.isFile } ?: return null
    return try {
        readme.useLines { lines ->
            lines
                .map { it.trim() }
                .filter { line ->
                    line.isNotBlank() &&
                        !line.startsWith("#") &&
                        !line.startsWith("![") &&
                        !line.startsWith("[!") &&
                        !line.startsWith("```") &&
                        !line.startsWith("<") &&
                        !line.startsWith("|") &&
                        !line.startsWith("---")
                }
                .firstOrNull()
                ?.take(240)
        }
    } catch (e: Exception) {
        logger.debug("[publish-buddy] failed to read README for ${project.path}: ${e.message}")
        null
    }
}

private val defaultDescription =
    project.readmeSummaryOrNull()
        ?: project.description?.takeIf { it.isNotBlank() }
        ?: "Say goodbye to template code and embrace simplicity and elegance"

plugins {
    id("com.vanniktech.maven.publish")
}

// 创建扩展并设置默认值
val publishExtension = createExtension<PublishConventionExtension>().apply {
    projectDescription.set(defaultDescription)
    authorName.set(Defaults.AUTHOR_NAME)
    gitUrl.set(Defaults.GIT_URL)
    emailDomain.set(Defaults.EMAIL_DOMAIN)
    enableAggregatePublishTasksByParentDir.set(false)
    licenseName.set(Defaults.LICENSE_NAME)
    licenseUrl.set(Defaults.LICENSE_URL)
    licenseDistribution.set(Defaults.LICENSE_URL)
}

// Git URL 解析工具函数
fun String.toGitHost() = substringAfter("://").substringBefore("/")
fun String.toGitRepoName() = substringAfter("://").substringAfter("/").removeSuffix(".git")
fun String.toGitBaseUrl() = removeSuffix(".git")
fun String.toPascalFromPath() =
    split(":")
        .filter { it.isNotBlank() }
        .joinToString("") { segment -> segment.replaceFirstChar { c -> c.uppercase() } }

private fun Project.publishBuddyCacheKey(): String = "${rootProject.projectDir.absolutePath}|$path"

/**
 * 只处理同一个 Gradle build 内部的 project 依赖。
 *
 * 外部 Maven 依赖会正常出现在 POM / Gradle metadata 里，
 * 但不应该由 publish-buddy 代发到 Maven Central。
 */
fun Project.hasProjectDependencies(): Boolean = directProjectDependencies().isNotEmpty()

private val directProjectDepsCache = mutableMapOf<String, Set<Project>>()
private val recursiveProjectDepsCache = mutableMapOf<String, Lazy<Set<Project>>>()
private val publishTaskPathsCache = mutableMapOf<String, Lazy<List<String>>>()

/**
 * 通过 Gradle model 提取 ProjectDependency，兼容：
 * - project(":path")
 * - typesafe project accessors（projects.foo.bar）
 * - convention plugin 在 configuration 上注入的 project 依赖
 */
fun Project.directProjectDependencies(): Set<Project> =
    directProjectDepsCache.getOrPut(publishBuddyCacheKey()) {
        configurations
            .asSequence()
            .filter { configuration -> configuration.dependencies.isNotEmpty() }
            .flatMap { configuration ->
                configuration.dependencies
                    .withType(ProjectDependency::class.java)
                    .asSequence()
            }
            .mapNotNull { dependency -> rootProject.findProject(dependency.path) }
            .filterNot { dependencyProject -> dependencyProject == this }
            .toCollection(linkedSetOf<Project>())
    }

fun Project.recursiveProjectDependencies(): Set<Project> {
    return recursiveProjectDepsCache
        .getOrPut(publishBuddyCacheKey()) {
            lazy(LazyThreadSafetyMode.NONE) {
                val visited = linkedSetOf<Project>()
                val queue = ArrayDeque<Project>()
                directProjectDependencies().forEach(queue::addLast)

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    if (!visited.add(current)) continue
                    if (!current.hasProjectDependencies()) continue
                    current.directProjectDependencies()
                        .filterNot { it == this || visited.contains(it) }
                        .forEach(queue::addLast)
                }
                visited
            }
        }
        .value
}

fun Project.publishTaskPathsForProjectDependencies(): List<String> {
    return publishTaskPathsCache
        .getOrPut(publishBuddyCacheKey()) {
            lazy(LazyThreadSafetyMode.NONE) {
                if (!hasProjectDependencies()) {
                    emptyList()
                } else {
                    recursiveProjectDependencies()
                        .mapNotNull { dep -> dep.tasks.findByName("publishToMavenCentral")?.path }
                        .distinct()
                }
            }
        }
        .value
}

fun Project.configureAggregatePublishTasksByParentDir(enabledProvider: Provider<Boolean>) {
    if (this != rootProject) return

    gradle.projectsEvaluated {
        if (!enabledProvider.getOrElse(false)) {
            logger.debug("[publish-buddy] aggregate publish tasks disabled (enableAggregatePublishTasksByParentDir=false)")
            return@projectsEvaluated
        }

        val grouped by lazy(LazyThreadSafetyMode.NONE) {
            subprojects
                .asSequence()
                .filter { it.path.startsWith(":lib") }
                .mapNotNull { child ->
                    val publishTaskPath = child.tasks.findByName("publishToMavenCentral")?.path ?: return@mapNotNull null
                    val parentPath = child.path.substringBeforeLast(":", "").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    parentPath to publishTaskPath
                }
                .groupBy({ (parentPath, _) -> parentPath }, { (_, publishTaskPath) -> publishTaskPath })
                .mapValues { (_, publishTaskPaths) -> publishTaskPaths.distinct() }
                .filterValues { publishTaskPaths -> publishTaskPaths.isNotEmpty() }
        }

        val shortNameCounts = grouped.keys
            .map { parentPath -> parentPath.substringAfterLast(":") }
            .groupingBy { it }
            .eachCount()

        grouped.forEach { (parentPath, publishTaskPaths) ->
            val canonicalTaskName = "publish${parentPath.toPascalFromPath()}ToMavenCentral"
            val existingCanonicalTask = tasks.findByName(canonicalTaskName)
            if (existingCanonicalTask == null) {
                tasks.register(canonicalTaskName) {
                    group = "publishing"
                    description = "Publish all modules directly under $parentPath"
                    dependsOn(publishTaskPaths)
                }
            } else {
                tasks.named(canonicalTaskName) {
                    group = "publishing"
                    description = "Publish all modules directly under $parentPath"
                    dependsOn(publishTaskPaths)
                }
            }

            val shortSegment = parentPath.substringAfterLast(":")
            val canCreateShortAlias = shortSegment.isNotBlank() && shortNameCounts[shortSegment] == 1
            if (canCreateShortAlias) {
                val aliasTaskName = "publish${shortSegment.replaceFirstChar { c -> c.uppercase() }}ToMavenCentral"
                if (aliasTaskName != canonicalTaskName && tasks.findByName(aliasTaskName) == null) {
                    tasks.register(aliasTaskName) {
                        group = "publishing"
                        description = "Alias of $canonicalTaskName"
                        dependsOn(canonicalTaskName)
                    }
                }
            }
        }
    }
}

// 仅对 publishToMavenCentral task 添加依赖推断，同时兼容任务稍后才被注册的场景。
tasks.matching { it.name == "publishToMavenCentral" }.configureEach {
    dependsOn(provider {
        if (!project.hasProjectDependencies()) {
            emptyList()
        } else {
            project.publishTaskPathsForProjectDependencies()
        }
    })
}

if (project == rootProject) {
    project.configureAggregatePublishTasksByParentDir(
        enabledProvider = publishExtension.enableAggregatePublishTasksByParentDir,
    )
}

// 延迟配置 mavenPublishing，确保用户配置已生效
afterEvaluate {
    val ext = publishExtension
    val gitUrl = ext.gitUrl.get()
    val gitHost = gitUrl.toGitHost()
    val gitRepoName = gitUrl.toGitRepoName()
    val gitBaseUrl = gitUrl.toGitBaseUrl()
    val authorName = ext.authorName.get()
    val authorEmail = "${authorName}@${ext.emailDomain.get()}"

    mavenPublishing {
        publishToMavenCentral(automaticRelease = true)
        // 只在有签名配置时才签名
        if (project.hasProperty("signing.keyId") ||
            project.hasProperty("signing.password") ||
            project.hasProperty("signing.secretKeyRingFile")) {
            signAllPublications()
        }
        coordinates(project.group.toString(), project.name, project.version.toString())

        pom {
            name.set(project.name)
            description.set(ext.projectDescription.get())
            inceptionYear.set(LocalDate.now().year.toString())
            url.set(gitBaseUrl)

            licenses {
                license {
                    name.set(ext.licenseName.get())
                    url.set(ext.licenseUrl.get())
                    distribution.set(ext.licenseDistribution.get())
                }
            }

            developers {
                developer {
                    id.set(authorName)
                    name.set(authorName)
                    email.set(authorEmail)
                }
            }

            scm {
                connection.set("scm:git:git://$gitHost/$gitRepoName.git")
                developerConnection.set("scm:git:ssh://$gitHost/$gitRepoName.git")
                url.set(gitBaseUrl)
            }
        }
    }
}

subprojects {
    if (!path.startsWith(":lib")) return@subprojects
    apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
}

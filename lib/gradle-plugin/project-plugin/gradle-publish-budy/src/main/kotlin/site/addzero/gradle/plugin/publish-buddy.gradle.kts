package site.addzero.gradle.plugin

import org.gradle.api.Project
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

private val hasProjectDepsCache = mutableMapOf<String, Boolean>()

/**
 * 通过文本扫描 build.gradle.kts 判断是否含有 project(":...") 依赖，
 * 避免遍历 configurations 带来的高开销。
 */
fun Project.hasProjectDependencies(): Boolean =
    hasProjectDepsCache.getOrPut(path) {
        val kts = file("build.gradle.kts")
        if (!kts.exists()) return@getOrPut false
        val regex = Regex("""project\s*\(\s*["':]""")
        var inBlockComment = false
        kts.useLines { lines -> lines.any { line ->
            val trimmed = line.trim()
            if (inBlockComment) {
                if ("*/" in trimmed) inBlockComment = false
                return@any false
            }
            if (trimmed.startsWith("/*")) {
                inBlockComment = "*/" !in trimmed
                return@any false
            }
            !trimmed.startsWith("//") && regex.containsMatchIn(trimmed)
        }}
    }

private val directProjectDepsCache = mutableMapOf<String, Set<Project>>()

/**
 * 通过文本扫描 build.gradle.kts 提取 project(":...") 依赖路径，
 * 完全避免遍历 configurations（KMP 项目 configuration 数量极多，遍历开销大）。
 */
fun Project.directProjectDependencies(): Set<Project> =
    directProjectDepsCache.getOrPut(path) {
        val kts = file("build.gradle.kts")
        if (!kts.exists()) return@getOrPut emptySet()
        val projectRefRegex = Regex("""project\s*\(\s*["'](:[^"']+)["']\s*\)""")
        var inBlockComment = false
        kts.useLines { lines ->
            lines.flatMap { line ->
                val trimmed = line.trim()
                if (inBlockComment) {
                    if ("*/" in trimmed) inBlockComment = false
                    return@flatMap emptySequence<String>()
                }
                if (trimmed.startsWith("/*")) {
                    inBlockComment = "*/" !in trimmed
                    return@flatMap emptySequence<String>()
                }
                if (trimmed.startsWith("//")) return@flatMap emptySequence<String>()
                projectRefRegex.findAll(trimmed).map { it.groupValues[1] }
            }.toList()
        }.mapNotNull { depPath -> rootProject.findProject(depPath) }.toSet()
    }

fun Project.recursiveProjectDependencies(): Set<Project> {
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
    return visited
}

fun Project.publishTaskPathsForProjectDependencies(): List<String> {
    if (!hasProjectDependencies()) return emptyList()
    return recursiveProjectDependencies()
        .mapNotNull { dep -> dep.tasks.findByName("publishToMavenCentral")?.path }
        .distinct()
}

fun Project.configureAggregatePublishTasksByParentDir(enabled: Boolean) {
    if (this != rootProject) return
    if (!enabled) {
        logger.debug("[publish-buddy] aggregate publish tasks disabled (enableAggregatePublishTasksByParentDir=false)")
        return
    }

    gradle.projectsEvaluated {
        val grouped = subprojects
            .groupBy { subProject -> subProject.path.substringBeforeLast(":", "") }
            .filterKeys { parentPath -> parentPath.isNotBlank() }
            .mapValues { (_, children) ->
                children
                    .mapNotNull { child -> child.tasks.findByName("publishToMavenCentral")?.path }
                    .distinct()
            }
            .filterValues { publishPaths -> publishPaths.isNotEmpty() }

        val shortNameCounts = grouped.keys
            .map { parentPath -> parentPath.substringAfterLast(":") }
            .groupingBy { it }
            .eachCount()

        grouped.forEach { (parentPath, publishTaskPaths) ->
            val canonicalTaskName = "publish${parentPath.toPascalFromPath()}ToMavenCentral"
            val canonicalTask = tasks.findByName(canonicalTaskName)
                ?: tasks.register(canonicalTaskName).get()
            canonicalTask.group = "publishing"
            canonicalTask.description = "Publish all modules directly under $parentPath"
            canonicalTask.dependsOn(publishTaskPaths)

            val shortSegment = parentPath.substringAfterLast(":")
            val canCreateShortAlias = shortSegment.isNotBlank() && shortNameCounts[shortSegment] == 1
            if (canCreateShortAlias) {
                val aliasTaskName = "publish${shortSegment.replaceFirstChar { c -> c.uppercase() }}ToMavenCentral"
                if (aliasTaskName != canonicalTaskName && tasks.findByName(aliasTaskName) == null) {
                    tasks.register(aliasTaskName) {
                        group = "publishing"
                        description = "Alias of $canonicalTaskName"
                        dependsOn(canonicalTask)
                    }
                }
            }
        }
    }
}

// 仅对 publishToMavenCentral task 添加依赖推断（避免 configureEach 遍历所有 task）
runCatching {
    tasks.named("publishToMavenCentral") {
        dependsOn(provider {
            if (!project.hasProjectDependencies()) {
                emptyList()
            } else {
                project.publishTaskPathsForProjectDependencies()
            }
        })
    }
}

project.configureAggregatePublishTasksByParentDir(
    enabled = publishExtension.enableAggregatePublishTasksByParentDir.getOrElse(false),
)

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
    // 排除不需要签名的项目
    if (path == ":lib:apt-dict-processor") return@subprojects
    apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
}


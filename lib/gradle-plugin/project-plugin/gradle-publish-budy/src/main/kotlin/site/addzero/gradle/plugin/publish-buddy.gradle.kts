package site.addzero.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
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

private val defaultDescription = project.description?.takeIf { it.isNotBlank() }
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

fun Project.directProjectDependencies(): Set<Project> =
    configurations
        .flatMap { cfg ->
            cfg.dependencies
                .withType(ProjectDependency::class.java)
                .mapNotNull { dep -> rootProject.findProject(dep.path) }
        }
        .toSet()

fun Project.recursiveProjectDependencies(): Set<Project> {
    val visited = linkedSetOf<Project>()
    val queue = ArrayDeque<Project>()
    directProjectDependencies().forEach(queue::addLast)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (!visited.add(current)) continue
        current.directProjectDependencies()
            .filterNot { it == this || visited.contains(it) }
            .forEach(queue::addLast)
    }
    return visited
}

fun Project.publishTaskPathsForProjectDependencies(): List<String> =
    recursiveProjectDependencies()
        .mapNotNull { dep -> dep.tasks.findByName("publishToMavenCentral")?.path }
        .distinct()

fun Project.configureAggregatePublishTasksByParentDir() {
    if (this != rootProject) return

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
            val canonicalTask = tasks.findByName(canonicalTaskName) ?: tasks.create(canonicalTaskName)
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

tasks.configureEach {
    if (name == "publishToMavenCentral") {
        dependsOn(provider { project.publishTaskPathsForProjectDependencies() })
    }
}

project.configureAggregatePublishTasksByParentDir()

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


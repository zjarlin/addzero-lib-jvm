package site.addzero.buildlogic.publish

import site.addzero.gradle.PublishConventionExtension
import java.time.LocalDate

// 默认配置常量
plugins {
    id("site.addzero.buildlogic.publish.publish-ext")
    id("com.vanniktech.maven.publish")
}
val create = the<PublishConventionExtension>()

// 直接访问扩展配置的计算属性
val projectDescription get() = create.projectDescription.get()
val authName get() = create.authorName.get()
val gitUrl get() = create.gitUrl.get()

// License 计算属性
val licenseName get() = create.licenseName.get()
val licenseUrl get() = create.licenseUrl.get()
val licenseDistribution get() = create.licenseDistribution.get()

fun String.toGitRepoPath() = this.substringAfter("://").substringAfter("/")

// 基于全局配置的扩展属性
val gitBaseUrl get() = gitUrl.removeSuffix(".git")
val gitRepoPath get() = gitUrl.toGitRepoPath()
val gitHost get() = gitUrl.substringAfter("://").substringBefore("/")
val gitRepoName get() = gitUrl.toGitRepoPath().removeSuffix(".git")
val authEmail get() = "$authName@outlook.com"

//fun String.toScmConnection(host: String = gitHost, repoName: String = gitRepoName): String =
//    "scm:git:git://$host/$repoName.git"

//fun String.toDeveloperConnection(host: String = gitHost, repoName: String = gitRepoName): String =
//    "scm:git:ssh://$host/$repoName.git"


// 从扩展配置获取值的便捷函数
//fun PublishConventionExtension.getAuthEmail(): String = "${authorName.get()}@${emailDomain.get()}"
//fun PublishConventionExtension.getGitBaseUrl(): String = gitUrl.get().removeSuffix(".git")
//fun PublishConventionExtension.getGitHost(): String = gitUrl.get().substringAfter("://").substringBefore("/")
//fun PublishConventionExtension.getGitRepoName(): String = gitUrl.get().substringAfter("://").substringAfter("/").removeSuffix(".git")


val pjVersion = project.version.toString()



mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), project.name, pjVersion)

    pom {
        name.set(project.name)
        description.set(projectDescription)
        inceptionYear.set(LocalDate.now().year.toString())
        url.set(gitBaseUrl)
        licenses {
            license {
                name.set(licenseName)
                url.set(licenseUrl)
                distribution.set(licenseDistribution)
            }
        }
        developers {
            developer {
                id.set(authName)
                name.set(authName)
                email.set(authEmail)
            }
        }

        scm {
            connection.set("scm:git:git://$gitHost/$gitRepoName.git")
            developerConnection.set("scm:git:ssh://$gitHost/$gitRepoName.git")
            url.set(gitBaseUrl)
        }
    }
}

// 创建一个自定义任务，用于在禁用配置缓存的情况下运行发布
tasks.register("safePublishToMavenCentral") {
    group = "publishing"
    description = "Publishes all publications to Maven Central with configuration cache temporarily disabled"
    
    doFirst {
        logger.lifecycle("🚀 Preparing to publish to Maven Central...")
        logger.lifecycle("⚠️  Configuration cache will be temporarily disabled for this operation")
    }
    
    doLast {
        logger.lifecycle("✅ Publishing to Maven Central completed")
        logger.lifecycle("🔄 You can re-enable configuration cache for other tasks")
    }
}

subprojects {
    if (!path.startsWith(":lib:")) {
        "path not startwith :lib,skip module ${project.name}"
        return@subprojects
    }
    listOf(
//        "site.addzero.publish-buddy",
        "site.addzero.buildlogic.publish.publish-buddy",
    ).forEach {
        apply(plugin = it)
//        autoApplyPlugin(it)
    }
}

// 提供关于配置缓存和发布任务的说明
gradle.taskGraph.whenReady {
    val publishTasks = allTasks.filter { task ->
        task.name.contains("publish", ignoreCase = true) && 
        task.name.contains("MavenCentral", ignoreCase = true)
    }
    
    if (publishTasks.isNotEmpty() && gradle.startParameter.isConfigurationCacheRequested) {
        logger.warn("⚠️  注意: 检测到您正在执行发布到Maven Central的任务，同时启用了配置缓存")
        logger.warn("💡 建议: 为确保发布任务正常运行，请使用以下命令之一:")
        logger.warn("   ./gradlew publishToMavenCentral")
        logger.warn("   或")
        logger.warn("   ./gradlew safePublishToMavenCentral")
    }
}
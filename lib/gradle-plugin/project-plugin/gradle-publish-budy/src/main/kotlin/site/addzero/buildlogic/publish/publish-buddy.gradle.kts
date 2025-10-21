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


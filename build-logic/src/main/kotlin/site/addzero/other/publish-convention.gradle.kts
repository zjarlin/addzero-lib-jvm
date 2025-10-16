package site.addzero.other

import org.gradle.api.provider.Property
import java.time.LocalDate

// 默认配置常量
val DEFAULT_PROJECT_DESCRIPTION = "addzero-kmp-scaffold"
val DEFAULT_AUTH_NAME = "zjarlin"
val DEFAULT_GIT_URL = "https://gitee.com/zjarlin/addzero.git"

// License 默认配置
val DEFAULT_LICENSE_NAME = "The Apache License, Version 2.0"
val DEFAULT_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
val DEFAULT_LICENSE_DISTRIBUTION = "http://www.apache.org/licenses/LICENSE-2.0.txt"

// 直接访问扩展配置的计算属性
val projectDescription get() = create.projectDescription.get()
val authName get() = create.authorName.get()
val gitUrl get() = create.gitUrl.get()

// License 计算属性
val licenseName get() = create.licenseName.get()
val licenseUrl get() = create.licenseUrl.get()
val licenseDistribution get() = create.licenseDistribution.get()

// Git URL 扩展函数
fun String.toGitBaseUrl() = this.removeSuffix(".git")
fun String.toGitRepoPath() = this.substringAfter("://").substringAfter("/")
fun String.toGitHost() = this.substringAfter("://").substringBefore("/")
fun String.toGitRepoName() = this.toGitRepoPath().removeSuffix(".git")

// 基于全局配置的扩展属性
val gitBaseUrl get() = gitUrl.toGitBaseUrl()
val gitRepoPath get() = gitUrl.toGitRepoPath()
val gitHost get() = gitUrl.toGitHost()
val gitRepoName get() = gitUrl.toGitRepoName()
val email get() = authName.toEmail()

// Author 相关扩展
fun String.toEmail() = "$this@outlook.com"
fun String.toScmConnection(host: String = gitHost, repoName: String = gitRepoName): String =
    "scm:git:git://$host/$repoName.git"
fun String.toDeveloperConnection(host: String = gitHost, repoName: String = gitRepoName): String =
    "scm:git:ssh://$host/$repoName.git"

interface PublishConventionExtension {
    val projectDescription: Property<String>
    val authorName: Property<String>
    val gitUrl: Property<String>
    val emailDomain: Property<String>

    // License 配置
    val licenseName: Property<String>
    val licenseUrl: Property<String>
    val licenseDistribution: Property<String>
}

val create = extensions.create<PublishConventionExtension>("addzeroPublishBuddy").apply {
    // 设置默认值
    projectDescription.set(DEFAULT_PROJECT_DESCRIPTION)
    authorName.set(DEFAULT_AUTH_NAME)
    gitUrl.set(DEFAULT_GIT_URL)
    emailDomain.set("outlook.com")

    // License 默认值
    licenseName.set(DEFAULT_LICENSE_NAME)
    licenseUrl.set(DEFAULT_LICENSE_URL)
    licenseDistribution.set(DEFAULT_LICENSE_DISTRIBUTION)
}

// 从扩展配置获取值的便捷函数
fun PublishConventionExtension.getEmail(): String = "${authorName.get()}@${emailDomain.get()}"
fun PublishConventionExtension.getGitBaseUrl(): String = gitUrl.get().removeSuffix(".git")
fun PublishConventionExtension.getGitHost(): String = gitUrl.get().substringAfter("://").substringBefore("/")
fun PublishConventionExtension.getGitRepoName(): String = gitUrl.get().substringAfter("://").substringAfter("/").removeSuffix(".git")



plugins {
    id("com.vanniktech.maven.publish")
}


// 注意：由于Maven Publish插件与Gradle配置缓存存在兼容性问题，
// 在使用publishToMavenLocal或publishToMavenCentral任务时，
// 请使用--no-configuration-cache参数禁用配置缓存
// 例如: ./gradlew publishToMavenLocal --no-configuration-cache

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
                email.set(email)
            }
        }

        scm {
            connection.set("scm:git:git://$gitHost/$gitRepoName.git")
            developerConnection.set("scm:git:ssh://$gitHost/$gitRepoName.git")
            url.set(gitBaseUrl)
        }
    }
}

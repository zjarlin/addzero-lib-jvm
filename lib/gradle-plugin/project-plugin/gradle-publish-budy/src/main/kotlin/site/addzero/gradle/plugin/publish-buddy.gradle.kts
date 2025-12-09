package site.addzero.gradle.plugin

import site.addzero.gradle.PublishConventionExtension
import site.addzero.util.createExtension
import java.time.LocalDate

// 默认配置常量
private object Defaults {
    const val PROJECT_DESCRIPTION = "addzero-kmp-scaffold"
    const val AUTHOR_NAME = "zjarlin"
    const val GIT_URL = "https://gitee.com/zjarlin/addzero.git"
    const val EMAIL_DOMAIN = "outlook.com"
    const val LICENSE_NAME = "The Apache License, Version 2.0"
    const val LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
}

plugins {
    id("com.vanniktech.maven.publish")
}

// 创建扩展并设置默认值
val publishExtension = createExtension<PublishConventionExtension>().apply {
    projectDescription.set(Defaults.PROJECT_DESCRIPTION)
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
    if (!path.startsWith(":lib:")) return@subprojects
    // 排除不需要签名的项目
    if (path == ":lib:apt-dict-processor") return@subprojects
    apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
}


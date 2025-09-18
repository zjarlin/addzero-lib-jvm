import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.text.SimpleDateFormat
import java.util.*

/**
 * Publish Convention Plugin
 *
 * 该插件提供了一种简便的方式来配置Maven发布，只需提供几个参数即可：
 * - projectDescription: 项目描述
 * - gitUrl: Git仓库URL
 * - licenseName: 许可证名称
 * - licenseUrl: 许可证URL
 * - developerEmail: 开发者邮箱
 *
 * 使用示例:
 * ```
 * publishConfig {
 *     projectDescription = "你的项目描述"
 *     gitUrl = "https://github.com/user/repo.git"
 *     licenseName = "The Apache License, Version 2.0"
 *     licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
 *     developerEmail = "developer@example.com"
 * }
 * ```
 */
class PublishConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 应用Maven Publish插件
        project.pluginManager.apply("com.vanniktech.maven.publish")

        // 创建扩展配置
        val extension = PublishPluginExtension.create(project)
        // 配置Maven发布
        val version = SimpleDateFormat(
            "yyyy.MM.dd", Locale.getDefault()
        ).format(Date())
        project.extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(true)
            signAllPublications()
            coordinates(
                project.group.toString(), project.name, version
            )

            pom {
                name.set(project.name)
                description.set(extension.projectDescription)
                inceptionYear.set(project.provider { Calendar.getInstance().get(Calendar.YEAR).toString() })

                // 从gitUrl解析相关参数
                val gitBaseUrl = extension.gitUrl.map { it.removeSuffix(".git") }
                val gitHost = extension.gitUrl.map { it.substringAfter("://").substringBefore("/") }
                val gitRepoName =
                    extension.gitUrl.map { it.substringAfter("://").substringAfter("/").removeSuffix(".git") }

                url.set(gitBaseUrl)

                licenses {
                    license {
                        name.set(extension.licenseName)
                        url.set(extension.licenseUrl)
                        distribution.set(extension.licenseUrl)
                    }
                }

                developers {
                    developer {
                        // 从邮箱提取用户名作为开发者ID和名称
                        val authName = extension.developerEmail.map { it.substringBefore("@") }
                        id.set(authName)
                        name.set(authName)
                        email.set(extension.developerEmail)
                    }
                }

                scm {
                    connection.set(gitHost.zip(gitRepoName) { host, repo -> "scm:git:git://$host/$repo.git" })
                    developerConnection.set(gitHost.zip(gitRepoName) { host, repo -> "scm:git:ssh://$host/$repo.git" })
                    url.set(gitBaseUrl)
                }
            }
        }
    }
}

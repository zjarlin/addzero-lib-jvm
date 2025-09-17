import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Publish插件扩展，用于配置Maven发布参数
 */
interface PublishPluginExtension {
    /**
     * 项目描述
     */
    val projectDescription: Property<String>

    /**
     * Git仓库URL
     */
    val gitUrl: Property<String>

    /**
     * 许可证名称
     */
    val licenseName: Property<String>

    /**
     * 许可证URL
     */
    val licenseUrl: Property<String>

    /**
     * 开发者邮箱
     */
    val developerEmail: Property<String>

    companion object {
        fun create(project: Project): PublishPluginExtension {
            return project.extensions.create("publishConfig", PublishPluginExtension::class.java).apply {
                projectDescription.convention("addzero全栈脚手架")
                gitUrl.convention("https://gitee.com/zjarlin/addzero.git")
                licenseName.convention("The Apache License, Version 2.0")
                licenseUrl.convention("http://www.apache.org/licenses/LICENSE-2.0.txt")
                developerEmail.convention("zjarlin@outlook.com")
            }
        }
    }
}

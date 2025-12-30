package site.addzero.kcp.reified.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Gradle 插件 - 用于注册 K2 编译器插件
 *
 * 注意：这个插件仅在发布到 Maven 仓库后才能使用。
 * 对于项目内开发，需要使用 included build 或 composite build 方式。
 */
class ReifiedGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        // 验证插件应用
        target.logger.lifecycle("[ReifiedGradlePlugin] Plugin applied to ${target.name}")
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = "site.addzero.kcp.reified"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "site.addzero",
        artifactId = "kcp-reified-plugin",
        version = "2025.12.34"
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        // 自动添加注解依赖
        kotlinCompilation.dependencies {
            implementation("site.addzero:kcp-reified-annotations:2025.12.34")

        }

        return project.provider { emptyList() }
    }
}

package site.addzero.gradle.plugin.kspbuddy

import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.mapProperty

/**
 * KspBuddy插件的扩展类，用于声明KSP处理器必须的上下文
 */
open class KspBuddyExtension(project: Project) {
    /**
     * KSP处理器必须的上下文参数映射
     * 来源可以是配置文件或直接声明
     */
    val mustMap: MapProperty<String, String> = project.objects.mapProperty<String, String>().apply {
        convention(mapOf( ))
    }
}

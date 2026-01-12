package site.addzero.util.lsi_impl.impl.kt.project

import com.google.gson.JsonObject
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import site.addzero.util.lsi.assist.TypeChecker
import site.addzero.util.lsi.assist.getDefaultValueForType
import site.addzero.util.lsi_impl.impl.intellij.project.findKtClassByName
import site.addzero.util.lsi_impl.impl.kt.clazz.ktClassToJson

/**
 * Helper: 创建 List 类型的 JSON 内容
 */
fun Project.createListJson(elementType: String): JsonObject {
    val listJson = JsonObject()
    if (TypeChecker.isCustomObjectType(elementType)) {
        val elementClass = findKtClassByName(elementType)
        elementClass?.let { listJson.add("element", it.ktClassToJson()) }
    } else {
        listJson.addProperty("element", getDefaultValueForType(elementType))
    }
    return listJson
}

/**
 * 获取项目Kotlin版本信息
 */
fun Project.getKotlinVersion(): String? {
    // 获取项目中的Kotlin插件版本
    val plugin = PluginManagerCore.getPlugin(
        PluginId.getId("org.jetbrains.kotlin")
    )
    return plugin?.version
}

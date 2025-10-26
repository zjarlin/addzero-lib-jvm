package site.addzero.ide.config.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import java.io.File

/**
 * 项目级配置持久化管理器
 * 负责项目特定配置数据的保存和加载
 */
@Service(Service.Level.PROJECT)
@State(name = "AutoDDLProjectConfig", storages = [Storage("autoddL-project-config.xml")])
class ProjectConfigPersistenceManager(private val project: Project) : SimplePersistentStateComponent<ProjectConfigState>(ProjectConfigState()) {
    
    companion object {
        fun getInstance(project: Project): ProjectConfigPersistenceManager {
            return project.getService(ProjectConfigPersistenceManager::class.java)
        }
    }
    
    /**
     * 保存项目配置数据
     *
     * @param configKey 配置键
     * @param configData 配置数据
     */
    fun saveConfig(configKey: String, configData: Map<String, Any?>) {
        val configJson = getObjectMapper().writeValueAsString(configData)
        state.configData[configKey] = configJson
    }
    
    /**
     * 加载项目配置数据
     *
     * @param configKey 配置键
     * @return 配置数据
     */
    fun loadConfig(configKey: String): Map<String, Any?> {
        val configJson = state.configData[configKey] ?: return emptyMap()
        return try {
            getObjectMapper().readValue(configJson)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * 删除项目配置数据
     *
     * @param configKey 配置键
     */
    fun removeConfig(configKey: String) {
        state.configData.remove(configKey)
    }
    
    /**
     * 导出配置到文件
     *
     * @param configKey 配置键
     * @param filePath 文件路径
     */
    fun exportConfig(configKey: String, filePath: String) {
        val configData = loadConfig(configKey)
        val json = getObjectMapper().writeValueAsString(configData)
        File(filePath).writeText(json)
    }
    
    /**
     * 从文件导入配置
     *
     * @param configKey 配置键
     * @param filePath 文件路径
     */
    fun importConfig(configKey: String, filePath: String) {
        try {
            val json = File(filePath).readText()
            val configData: Map<String, Any?> = getObjectMapper().readValue(json)
            saveConfig(configKey, configData)
        } catch (e: Exception) {
            throw RuntimeException("Failed to import config from $filePath", e)
        }
    }
    
    private fun getObjectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }
}

/**
 * 项目配置状态类
 */
class ProjectConfigState : BaseState() {
    var configData by map<String, String>()
}
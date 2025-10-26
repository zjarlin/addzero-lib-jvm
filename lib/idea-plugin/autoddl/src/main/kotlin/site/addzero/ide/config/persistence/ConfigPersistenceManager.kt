package site.addzero.ide.config.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import java.io.File

/**
 * 配置持久化管理器
 * 负责配置数据的保存和加载
 */
@Service(Service.Level.APP)
@State(name = "AutoDDLConfig", storages = [Storage("autoddL-config.xml")])
class ConfigPersistenceManager : SimplePersistentStateComponent<ConfigState>(ConfigState()) {
    
    companion object {
        fun getInstance(): ConfigPersistenceManager {
            return ApplicationManager.getApplication().getService(ConfigPersistenceManager::class.java)
        }
    }
    
    /**
     * 保存配置数据
     *
     * @param configKey 配置键
     * @param configData 配置数据
     */
    fun saveConfig(configKey: String, configData: Map<String, Any?>) {
        val configJson = getObjectMapper().writeValueAsString(configData)
        state.configData[configKey] = configJson
    }
    
    /**
     * 加载配置数据
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
     * 删除配置数据
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
 * 配置状态类
 */
class ConfigState : BaseState() {
    var configData by map<String, String>()
}
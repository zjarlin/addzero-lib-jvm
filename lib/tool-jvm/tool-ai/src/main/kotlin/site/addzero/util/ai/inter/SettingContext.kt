package site.addzero.util.ai.inter

/**
 * 设置上下文接口
 * 用于管理AI工具的设置信息，需要由用户实现
 */
interface SettingContext {
    /**
     * 获取设置信息
     */
    val settings: Settings

}

/**
 * 设置数据接口
 * 包含模型相关的配置信息
 */
interface Settings {
    /**
     * 模型制造商
     */
    val modelManufacturer: String

    /**
     * 离线模型名称
     */
    val modelNameOffline: String

    /**
     * 在线模型名称
     */
    val modelNameOnline: String

    /**
     * 模型密钥
     */
    val modelKey: String

    /**
     * Ollama URL
     */
    val ollamaUrl: String
}

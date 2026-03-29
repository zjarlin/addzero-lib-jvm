package site.addzero.system.spi.config

import site.addzero.system.model.common.PageResult
import site.addzero.system.model.dto.ConfigDTO
import site.addzero.system.model.dto.ConfigQuery

/**
 * 系统配置服务SPI
 * 提供配置的增删改查及缓存管理功能
 */
interface SystemConfigSpi {

    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，不存在返回默认值
     */
    fun getString(key: String, defaultValue: String? = null): String?

    /**
     * 获取整数配置
     */
    fun getInt(key: String, defaultValue: Int = 0): Int

    /**
     * 获取布尔配置
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    /**
     * 获取配置对象（JSON反序列化）
     */
    fun <T> getObject(key: String, clazz: Class<T>): T?

    /**
     * 设置配置值
     * @return 配置ID
     */
    fun set(key: String, value: String, description: String? = null): String

    /**
     * 批量设置配置
     */
    fun setBatch(configs: Map<String, String>)

    /**
     * 删除配置
     */
    fun delete(key: String)

    /**
     * 批量删除配置
     */
    fun deleteBatch(keys: List<String>)

    /**
     * 根据ID获取配置
     */
    fun getById(id: String): ConfigDTO?

    /**
     * 根据Key获取配置
     */
    fun getByKey(key: String): ConfigDTO?

    /**
     * 分页查询配置
     */
    fun page(query: ConfigQuery): PageResult<ConfigDTO>

    /**
     * 获取指定分类的所有配置
     */
    fun listByCategory(category: String): List<ConfigDTO>

    /**
     * 获取所有配置（Map形式）
     */
    fun getAll(): Map<String, String>

    /**
     * 检查配置是否存在
     */
    fun exists(key: String): Boolean

    /**
     * 刷新配置缓存
     */
    fun refresh()

    /**
     * 导出配置
     */
    fun export(): String

    /**
     * 导入配置
     */
    fun import(configJson: String): Int
}

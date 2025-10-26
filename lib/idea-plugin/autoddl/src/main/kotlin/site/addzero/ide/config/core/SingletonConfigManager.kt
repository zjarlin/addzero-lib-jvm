package site.addzero.ide.config.core

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * 单例配置管理器
 * 确保每个配置类只有一个实例，并提供全局访问点
 */
object SingletonConfigManager {
    private val configInstances = mutableMapOf<KClass<*>, Any>()

    /**
     * 获取配置实例（单例）
     *
     * @param configClass 配置类的KClass
     * @return 配置实例
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getConfig(configClass: KClass<T>): T {
        return configInstances.getOrPut(configClass) {
            // 使用无参构造函数创建实例
            configClass.constructors.firstOrNull { it.parameters.isEmpty() }
                ?.call()
                ?: throw IllegalArgumentException("无法创建配置类实例: ${configClass.simpleName}，缺少无参构造函数")
        } as T
    }

    /**
     * 更新配置实例
     *
     * @param configClass 配置类的KClass
     * @param instance 新的配置实例
     */
    fun <T : Any> updateConfig(configClass: KClass<T>, instance: T) {
        configInstances[configClass] = instance
    }

    /**
     * 清空所有配置实例（用于测试或重置）
     */
    fun clearAllConfigs() {
        configInstances.clear()
    }
}

/**
 * 配置委托属性，用于通过类名.字段名方式访问配置项
 */
object ConfigDelegates {
    /**
     * 通过类名.字段名方式获取配置值
     *
     * @param configClass 配置类
     * @param fieldName 字段名
     * @return 配置值
     */
    fun <T : Any> getValue(configClass: KClass<T>, fieldName: String): Any? {
        val configInstance = SingletonConfigManager.getConfig(configClass)
        val property = configClass.memberProperties.find { it.name == fieldName }
            ?: throw IllegalArgumentException("配置类 ${configClass.simpleName} 中未找到字段: $fieldName")
        
        return property.call(configInstance)
    }
    
    /**
     * 通过属性引用获取配置值
     *
     * @param property 配置属性引用
     * @return 配置值
     */
    fun <T : Any, R> getValue(property: kotlin.reflect.KProperty1<T, R>): R {
        @Suppress("UNCHECKED_CAST")
        val configClass = property.parameters[0].type.classifier as KClass<T>
        val configInstance = SingletonConfigManager.getConfig(configClass)
        return property.getter.call(configInstance)
    }
}
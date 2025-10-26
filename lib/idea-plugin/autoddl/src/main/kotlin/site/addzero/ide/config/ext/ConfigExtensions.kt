package site.addzero.ide.config.ext

import site.addzero.ide.config.core.ConfigDelegates
import site.addzero.ide.config.core.SingletonConfigManager
import kotlin.reflect.KClass

/**
 * 配置扩展函数和属性委托，提供更便捷的配置访问方式
 */

/**
 * 通过类名.字段名方式访问配置值的扩展属性
 *
 * 使用示例：
 * val host = DatabaseConfig::host.value
 */
val <T : Any, R> kotlin.reflect.KProperty1<T, R>.value: R
    get() = ConfigDelegates.getValue(this)

/**
 * 通过类和字段名获取配置值的辅助方法
 */
fun <T : Any> getConfigValue(configClass: KClass<T>, fieldName: String): Any? {
    return ConfigDelegates.getValue(configClass, fieldName)
}

/**
 * 获取配置实例的扩展属性
 *
 * 使用示例：
 * val dbConfig = DatabaseConfig.instance
 */
val <T : Any> KClass<T>.instance: T
    get() = SingletonConfigManager.getConfig(this)

/**
 * 更新配置实例的扩展函数
 *
 * 使用示例：
 * DatabaseConfig.updateInstance(newConfig)
 */
fun <T : Any> KClass<T>.updateInstance(instance: T) {
    SingletonConfigManager.updateConfig(this, instance)
}
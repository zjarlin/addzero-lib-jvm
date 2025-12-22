package site.addzero.ioc.registry

import site.addzero.ioc.registry.GlobalBeanRegistry.injectList
import kotlin.reflect.KClass

/**
 * Bean 注册表接口，支持通过 KClass 获取 Bean 实例
 * 主要用于策略模式，避免手动编写工厂类
 */
interface BeanRegistry {
    /**
     * 根据类型获取 Bean 实例
     * @param T Bean 类型
     * @param clazz Bean 的 KClass
     * @return Bean 实例，如果不存在则返回 null
     */
    fun <T : Any> getBean(clazz: KClass<T>): T?

    /**
     * 根据类型获取 Bean 实例，如果不存在则抛出异常
     * @param T Bean 类型
     * @param clazz Bean 的 KClass
     * @return Bean 实例
     * @throws IllegalArgumentException 如果 Bean 不存在
     */
    fun <T : Any> getRequiredBean(clazz: KClass<T>): T {
        return getBean(clazz) ?: throw IllegalArgumentException("No bean found for type")
    }

    /**
     * 注册 Bean 实例
     * @param T Bean 类型
     * @param clazz Bean 的 KClass
     * @param instance Bean 实例
     */
    fun <T : Any> registerBean(clazz: KClass<T>, instance: T)

    /**
     * 注册 Bean 提供者
     * @param T Bean 类型
     * @param clazz Bean 的 KClass
     * @param provider Bean 提供者函数
     */
    fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T)

    /**
     * 检查是否包含指定类型的 Bean
     * @param clazz Bean 的 KClass
     * @return 如果包含则返回 true
     */
    fun containsBean(clazz: KClass<*>): Boolean

    /**
     * 获取所有已注册的 Bean 类型
     * @return 所有已注册的 Bean 类型的集合
     */
    fun getBeanTypes(): Set<KClass<*>>

    /**
     * 获取指定接口或父类的所有实现
     * @param T 接口或父类类型
     * @param clazz 接口或父类的 KClass
     * @return 所有实现类的实例列表
     */
    fun <T : Any> injectList(clazz: KClass<T>): List<T>


}

inline fun <reified T : Any> getSupportStrategty(predicate: (T) -> Boolean): T? {
    return injectList(T::class).firstOrNull(predicate)
}

/**
 * 默认的 Bean 注册表实现
 */
class DefaultBeanRegistry : BeanRegistry {
    private val beanMap = mutableMapOf<KClass<*>, Any>()
    private val providerMap = mutableMapOf<KClass<*>, () -> Any>()

    // 存储接口/父类与实现类的关系
    private val implementationMap = mutableMapOf<KClass<*>, Set<KClass<*>>>()

    override fun <T : Any> getBean(clazz: KClass<T>): T? {
        // 先尝试从已创建的实例中获取
        @Suppress("UNCHECKED_CAST")
        val instance = beanMap[clazz] as? T
        if (instance != null) {
            return instance
        }

        // 如果没有实例，尝试从提供者创建
        val provider = providerMap[clazz] as? (() -> T)
        if (provider != null) {
            val newInstance = provider()
            beanMap[clazz] = newInstance
            return newInstance
        }

        return null
    }

    override fun <T : Any> registerBean(clazz: KClass<T>, instance: T) {
        beanMap[clazz] = instance
    }

    override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) {
        providerMap[clazz] = provider
    }

    override fun containsBean(clazz: KClass<*>): Boolean {
        return beanMap.containsKey(clazz) || providerMap.containsKey(clazz)
    }

    override fun getBeanTypes(): Set<KClass<*>> {
        return (beanMap.keys + providerMap.keys).toSet()
    }

    override fun <T : Any> injectList(clazz: KClass<T>): List<T> {
        val implementationClasses = implementationMap[clazz] ?: emptySet()

        @Suppress("UNCHECKED_CAST")
        return implementationClasses.mapNotNull { implClass ->
            getBean(implClass) as? T
        }
    }

    /**
     * 注册实现类到接口/父类的映射
     * 这个方法由生成的代码调用
     */
    internal fun <T : Any, R : T> registerImplementation(
        interfaceClass: KClass<T>,
        implementationClass: KClass<R>
    ) {
        val currentImplementations = implementationMap[interfaceClass]?.toMutableSet() ?: mutableSetOf()
        currentImplementations.add(implementationClass)
        implementationMap[interfaceClass] = currentImplementations
    }
}

/**
 * 全局 Bean 注册表实例
 */
object GlobalBeanRegistry : BeanRegistry by DefaultBeanRegistry()

/**
 * 扩展函数，方便获取 Bean
 */
inline fun <reified T : Any> BeanRegistry.getBean(): T? = getBean(T::class)

/**
 * 扩展函数，方便获取必需的 Bean
 */
inline fun <reified T : Any> BeanRegistry.getRequiredBean(): T = getRequiredBean(T::class)

/**
 * 扩展函数，方便注册 Bean
 */
inline fun <reified T : Any> BeanRegistry.registerBean(instance: T) = registerBean(T::class, instance)

/**
 * 扩展函数，方便注册 Bean 提供者
 */
inline fun <reified T : Any> BeanRegistry.registerProvider(noinline provider: () -> T) =
    registerProvider(T::class, provider)

/**
 * 扩展函数，方便注入指定类型的所有实现
 */
inline fun <reified T : Any> BeanRegistry.injectList(): List<T> = injectList(T::class)
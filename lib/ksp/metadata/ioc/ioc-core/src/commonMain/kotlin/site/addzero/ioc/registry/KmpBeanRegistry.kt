package site.addzero.ioc.registry

import kotlin.reflect.KClass

/**
 * KMP 兼容的 Bean 注册表实现
 * 支持接口实现关系的注册
 */
class KmpBeanRegistry : BeanRegistry {
    private val beanMap = mutableMapOf<KClass<*>, Any>()
    private val providerMap = mutableMapOf<KClass<*>, () -> Any>()

    // 存储接口/父类与实现类的关系
    private val implementationMap = mutableMapOf<KClass<*>, MutableSet<KClass<*>>>()

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

    override fun <T : Any> getRequiredBean(clazz: KClass<T>): T {
        return getBean(clazz) ?: throw IllegalArgumentException("No bean found for type: ${clazz.simpleName}")
    }

    override fun <T : Any> registerBean(clazz: KClass<T>, instance: T) {
        beanMap[clazz] = instance
    }

    override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) {
        providerMap[clazz] = provider
    }

    /**
     * 注册实现类到接口/父类的映射
     * 这个方法改为 public，供生成的代码使用
     */
    fun <T : Any, R : T> registerImplementation(
        interfaceClass: KClass<T>,
        implementationClass: KClass<R>
    ) {
        val currentImplementations = implementationMap.getOrPut(interfaceClass) { mutableSetOf() }
        currentImplementations.add(implementationClass)
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
}
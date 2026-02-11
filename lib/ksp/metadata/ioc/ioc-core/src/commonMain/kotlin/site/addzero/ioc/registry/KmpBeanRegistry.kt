package site.addzero.ioc.registry

import kotlin.reflect.KClass

/**
 * KMP 兼容的 Bean 注册表实现
 * 支持接口实现关系、泛型 Bean、循环依赖检测
 */
class KmpBeanRegistry : BeanRegistry {
    private val beanMap = mutableMapOf<KClass<*>, Any>()
    private val providerMap = mutableMapOf<KClass<*>, () -> Any>()
    private val nameMap = mutableMapOf<String, Any>()
    private val nameProviderMap = mutableMapOf<String, () -> Any>()
    private val extensionMap = mutableMapOf<KClass<*>, MutableMap<String, Any.() -> Any?>>()
    private val implementationMap = mutableMapOf<KClass<*>, MutableSet<KClass<*>>>()

    // 泛型 Bean 存储
    private val typedBeanMap = mutableMapOf<TypeKey, Any>()
    private val typedProviderMap = mutableMapOf<TypeKey, () -> Any>()

    // 循环依赖检测：正在创建中的 bean
    private val creating = mutableSetOf<Any>() // KClass 或 TypeKey 或 String

    private fun <T> withCircularCheck(key: Any, block: () -> T): T {
        if (key in creating) {
            throw IllegalStateException("循环依赖: $key 正在创建中。依赖链: ${creating.joinToString(" -> ")} -> $key")
        }
        creating.add(key)
        try {
            return block()
        } finally {
            creating.remove(key)
        }
    }

    override fun <T : Any> getBean(clazz: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        val instance = beanMap[clazz] as? T
        if (instance != null) return instance

        @Suppress("UNCHECKED_CAST")
        val provider = providerMap[clazz] as? (() -> T) ?: return null
        return withCircularCheck(clazz) {
            val newInstance = provider()
            beanMap[clazz] = newInstance
            newInstance
        }
    }

    override fun <T : Any> getRequiredBean(clazz: KClass<T>): T {
        return getBean(clazz) ?: throw IllegalArgumentException("No bean found for type: ${clazz.simpleName}")
    }

    override fun getBean(name: String): Any? {
        val instance = nameMap[name]
        if (instance != null) return instance

        val provider = nameProviderMap[name] ?: return null
        return withCircularCheck(name) {
            val newInstance = provider()
            nameMap[name] = newInstance
            newInstance
        }
    }

    // ============ 泛型 Bean ============

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBean(typeKey: TypeKey): T? {
        val instance = typedBeanMap[typeKey] as? T
        if (instance != null) return instance

        val provider = typedProviderMap[typeKey] as? (() -> T) ?: return null
        return withCircularCheck(typeKey) {
            val newInstance = provider()
            typedBeanMap[typeKey] = newInstance
            newInstance
        }
    }

    override fun <T : Any> registerBean(typeKey: TypeKey, instance: T) {
        typedBeanMap[typeKey] = instance
    }

    override fun <T : Any> registerProvider(typeKey: TypeKey, provider: () -> T) {
        typedProviderMap[typeKey] = provider
    }

    // ============ 基础注册 ============

    override fun <T : Any> registerBean(clazz: KClass<T>, instance: T) {
        beanMap[clazz] = instance
    }

    override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) {
        providerMap[clazz] = provider
    }

    override fun <T : Any> registerProvider(name: String, clazz: KClass<T>, provider: () -> T) {
        providerMap[clazz] = provider
        nameProviderMap[name] = provider
    }

    // ============ 扩展函数 ============

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any> registerExtension(receiverClass: KClass<R>, name: String, extension: R.() -> Any?) {
        val map = extensionMap.getOrPut(receiverClass) { mutableMapOf() }
        map[name] = extension as Any.() -> Any?
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any> getExtensions(receiverClass: KClass<R>): Map<String, R.() -> Any?> {
        return (extensionMap[receiverClass] ?: emptyMap()) as Map<String, R.() -> Any?>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any> getExtension(receiverClass: KClass<R>, name: String): (R.() -> Any?)? {
        return extensionMap[receiverClass]?.get(name) as? (R.() -> Any?)
    }

    // ============ 接口实现关系 ============

    fun <T : Any, R : T> registerImplementation(
        interfaceClass: KClass<T>,
        implementationClass: KClass<R>
    ) {
        implementationMap.getOrPut(interfaceClass) { mutableSetOf() }.add(implementationClass)
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
        return implementationClasses.mapNotNull { implClass -> getBean(implClass) as? T }
    }
}

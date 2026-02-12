package site.addzero.ioc.registry

import kotlin.reflect.KClass

/**
 * KMP-compatible BeanRegistry implementation.
 * Supports interface-implementation mapping, generic beans, circular dependency detection.
 */
class KmpBeanRegistry : MutableBeanRegistry {
    private val beanMap = mutableMapOf<KClass<*>, Any>()
    private val providerMap = mutableMapOf<KClass<*>, () -> Any>()
    private val nameMap = mutableMapOf<String, Any>()
    private val nameProviderMap = mutableMapOf<String, () -> Any>()
    private val extensionMap = mutableMapOf<KClass<*>, MutableMap<String, Any.() -> Any?>>()
    private val implementationMap = mutableMapOf<KClass<*>, MutableSet<KClass<*>>>()
    private val typedBeanMap = mutableMapOf<TypeKey, Any>()
    private val typedProviderMap = mutableMapOf<TypeKey, () -> Any>()
    private val tagMap = mutableMapOf<KClass<*>, MutableSet<String>>()

    // circular dependency detection
    private val creating = mutableSetOf<Any>()

    private fun <T> withCircularCheck(key: Any, block: () -> T): T {
        if (key in creating) {
            throw IllegalStateException("Circular dependency: $key is being created. Chain: ${creating.joinToString(" -> ")} -> $key")
        }
        creating.add(key)
        try {
            return block()
        } finally {
            creating.remove(key)
        }
    }

    // ============ get by type ============

    override fun <T : Any> get(clazz: KClass<T>): T? {
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

    // ============ get by name ============

    override fun get(name: String): Any? {
        val instance = nameMap[name]
        if (instance != null) return instance

        val provider = nameProviderMap[name] ?: return null
        return withCircularCheck(name) {
            val newInstance = provider()
            nameMap[name] = newInstance
            newInstance
        }
    }

    // ============ get by TypeKey ============

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(typeKey: TypeKey): T? {
        val instance = typedBeanMap[typeKey] as? T
        if (instance != null) return instance

        val provider = typedProviderMap[typeKey] as? (() -> T) ?: return null
        return withCircularCheck(typeKey) {
            val newInstance = provider()
            typedBeanMap[typeKey] = newInstance
            newInstance
        }
    }

    // ============ getAll ============

    override fun <T : Any> getAll(clazz: KClass<T>): List<T> {
        val implClasses = implementationMap[clazz] ?: emptySet()
        @Suppress("UNCHECKED_CAST")
        return implClasses.mapNotNull { implClass -> get(implClass) as? T }
    }

    override fun <T : Any> getAll(clazz: KClass<T>, tag: String): List<T> {
        val implClasses = implementationMap[clazz] ?: emptySet()
        @Suppress("UNCHECKED_CAST")
        return implClasses
            .filter { tagMap[it]?.contains(tag) == true }
            .mapNotNull { implClass -> get(implClass) as? T }
    }

    // ============ register ============

    override fun <T : Any> register(clazz: KClass<T>, instance: T) {
        beanMap[clazz] = instance
    }

    override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) {
        providerMap[clazz] = provider
    }

    override fun <T : Any> registerProvider(name: String, clazz: KClass<T>, provider: () -> T) {
        providerMap[clazz] = provider
        nameProviderMap[name] = provider
    }

    override fun <T : Any> register(typeKey: TypeKey, instance: T) {
        typedBeanMap[typeKey] = instance
    }

    override fun <T : Any> registerProvider(typeKey: TypeKey, provider: () -> T) {
        typedProviderMap[typeKey] = provider
    }

    // ============ interface implementation ============

    override fun <T : Any, R : T> registerImplementation(interfaceClass: KClass<T>, implClass: KClass<R>) {
        implementationMap.getOrPut(interfaceClass) { mutableSetOf() }.add(implClass)
    }

    // ============ extensions ============

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

    // ============ tags ============

    override fun <T : Any> tagBean(clazz: KClass<T>, tags: List<String>) {
        if (tags.isNotEmpty()) {
            tagMap.getOrPut(clazz) { mutableSetOf() }.addAll(tags)
        }
    }

    // ============ query ============

    override fun contains(clazz: KClass<*>): Boolean {
        return beanMap.containsKey(clazz) || providerMap.containsKey(clazz)
    }

    override fun types(): Set<KClass<*>> {
        return (beanMap.keys + providerMap.keys).toSet()
    }

    override fun clear() {
        beanMap.clear()
        providerMap.clear()
        nameMap.clear()
        nameProviderMap.clear()
        extensionMap.clear()
        implementationMap.clear()
        typedBeanMap.clear()
        typedProviderMap.clear()
        tagMap.clear()
        creating.clear()
    }
}

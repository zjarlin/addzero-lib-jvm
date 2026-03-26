package site.addzero.ioc.registry

import kotlin.reflect.KClass

/**
 * User-facing read-only interface for IoC container.
 * Only exposes retrieval methods — registration is internal.
 */
interface BeanRegistry {

    fun <T : Any> getBean(clazz: KClass<T>): T?

    fun getBean(name: String): Any?

    fun <T : Any> getBean(typeKey: TypeKey): T?

    fun <T : Any> injectList(clazz: KClass<T>): List<T>

    fun beanDefinitions(): List<BeanDefinition>

    fun beanDefinitions(tag: String): List<BeanDefinition>

    fun beanDefinition(name: String): BeanDefinition?

    fun <R : Any> getExtensions(receiverClass: KClass<R>): Map<String, R.() -> Any?>

    fun <R : Any> getExtension(receiverClass: KClass<R>, name: String): (R.() -> Any?)?

    fun <T : Any> injectList(clazz: KClass<T>, tag: String): List<T>

    fun contains(clazz: KClass<*>): Boolean

    fun types(): Set<KClass<*>>
}

/**
 * Internal mutable interface — used by generated code and SPI providers.
 * Not exposed to end users.
 */
interface MutableBeanRegistry : BeanRegistry {

    fun <T : Any> register(clazz: KClass<T>, instance: T)

    fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T)

    fun <T : Any> registerProvider(name: String, clazz: KClass<T>, provider: () -> T)

    fun <T : Any> register(typeKey: TypeKey, instance: T)

    fun <T : Any> registerProvider(typeKey: TypeKey, provider: () -> T)

    fun <T : Any, R : T> registerImplementation(interfaceClass: KClass<T>, implClass: KClass<R>)

    fun <R : Any> registerExtension(receiverClass: KClass<R>, name: String, extension: R.() -> Any?)

    fun <T : Any> registerDefinition(clazz: KClass<T>, definition: BeanDefinition)

    fun <T : Any> tagBean(clazz: KClass<T>, tags: List<String>)

    fun clear()
}

// ============================================================
// User-facing reified extensions (on BeanRegistry)
// ============================================================

inline fun <reified T : Any> BeanRegistry.getBean(): T? = getBean(T::class)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> BeanRegistry.getBean(name: String): T? = getBean(name) as? T

/** Get generic bean: `getBean<IService<User>>(User::class)` */
inline fun <reified T : Any> BeanRegistry.getBean(vararg generics: KClass<*>): T? =
    getBean(TypeKey.of(T::class, *generics))

inline fun <reified T : Any> BeanRegistry.require(): T =
    getBean(T::class) ?: throw IllegalArgumentException("No bean found for type: ${T::class.simpleName}")

inline fun <reified T : Any> BeanRegistry.require(name: String): T =
    getBean<T>(name) ?: throw IllegalArgumentException("No bean found for name: $name, type: ${T::class.simpleName}")

inline fun <reified T : Any> BeanRegistry.require(typeKey: TypeKey): T =
    getBean<T>(typeKey) ?: throw IllegalArgumentException("No bean found for typeKey: $typeKey")

/** Require generic bean: `require<IService<User>>(User::class)` */
inline fun <reified T : Any> BeanRegistry.require(vararg generics: KClass<*>): T =
    getBean<T>(*generics) ?: throw IllegalArgumentException("No bean: ${T::class.simpleName}<${generics.joinToString { it.simpleName ?: "?" }}>")

inline fun <reified T : Any> BeanRegistry.injectList(): List<T> = injectList(T::class)

inline fun <reified T : Any> BeanRegistry.injectList(tag: String): List<T> = injectList(T::class, tag)

fun BeanRegistry.findBeanDefinitions(tag: String): List<BeanDefinition> = beanDefinitions(tag)

fun BeanRegistry.findBeanDefinition(name: String): BeanDefinition? = beanDefinition(name)

inline fun <reified T : Any> BeanRegistry.contains(): Boolean = contains(T::class)

inline fun <reified R : Any> BeanRegistry.getExtensions(): Map<String, R.() -> Any?> = getExtensions(R::class)

inline fun <reified R : Any> BeanRegistry.getExtension(name: String): (R.() -> Any?)? = getExtension(R::class, name)

inline fun <reified R : Any> BeanRegistry.applyExtensions(receiver: R) {
    getExtensions<R>().values.forEach { ext -> receiver.ext() }
}

// ============================================================
// Internal reified extensions (on MutableBeanRegistry)
// ============================================================

inline fun <reified T : Any> MutableBeanRegistry.register(instance: T) = register(T::class, instance)

inline fun <reified T : Any> MutableBeanRegistry.tagBean(tags: List<String>) = tagBean(T::class, tags)

inline fun <reified T : Any> MutableBeanRegistry.registerDefinition(definition: BeanDefinition) =
    registerDefinition(T::class, definition)

inline fun <reified T : Any> MutableBeanRegistry.registerProvider(noinline provider: () -> T) =
    registerProvider(T::class, provider)

inline fun <reified R : Any> MutableBeanRegistry.registerExtension(name: String, noinline extension: R.() -> Any?) =
    registerExtension(R::class, name, extension)

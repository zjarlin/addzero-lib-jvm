package site.addzero.ioc.registry

import kotlin.reflect.KClass

/**
 * User-facing read-only interface for IoC container.
 * Only exposes retrieval methods — registration is internal.
 */
interface BeanRegistry {

    fun <T : Any> get(clazz: KClass<T>): T?

    fun get(name: String): Any?

    fun <T : Any> get(typeKey: TypeKey): T?

    fun <T : Any> getAll(clazz: KClass<T>): List<T>

    fun <R : Any> getExtensions(receiverClass: KClass<R>): Map<String, R.() -> Any?>

    fun <R : Any> getExtension(receiverClass: KClass<R>, name: String): (R.() -> Any?)?

    fun <T : Any> getAll(clazz: KClass<T>, tag: String): List<T>

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

    fun <T : Any> tagBean(clazz: KClass<T>, tags: List<String>)

    fun clear()
}

// ============================================================
// User-facing reified extensions (on BeanRegistry)
// ============================================================

inline fun <reified T : Any> BeanRegistry.get(): T? = get(T::class)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> BeanRegistry.get(name: String): T? = get(name) as? T

/** Get generic bean: `get<IService<User>>(User::class)` */
inline fun <reified T : Any> BeanRegistry.get(vararg generics: KClass<*>): T? =
    get(TypeKey.of(T::class, *generics))

inline fun <reified T : Any> BeanRegistry.require(): T =
    get(T::class) ?: throw IllegalArgumentException("No bean found for type: ${T::class.simpleName}")

inline fun <reified T : Any> BeanRegistry.require(name: String): T =
    get<T>(name) ?: throw IllegalArgumentException("No bean found for name: $name, type: ${T::class.simpleName}")

inline fun <reified T : Any> BeanRegistry.require(typeKey: TypeKey): T =
    get<T>(typeKey) ?: throw IllegalArgumentException("No bean found for typeKey: $typeKey")

/** Require generic bean: `require<IService<User>>(User::class)` */
inline fun <reified T : Any> BeanRegistry.require(vararg generics: KClass<*>): T =
    get<T>(*generics) ?: throw IllegalArgumentException("No bean: ${T::class.simpleName}<${generics.joinToString { it.simpleName ?: "?" }}>")

inline fun <reified T : Any> BeanRegistry.getAll(): List<T> = getAll(T::class)

inline fun <reified T : Any> BeanRegistry.getAll(tag: String): List<T> = getAll(T::class, tag)

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

inline fun <reified T : Any> MutableBeanRegistry.registerProvider(noinline provider: () -> T) =
    registerProvider(T::class, provider)

inline fun <reified R : Any> MutableBeanRegistry.registerExtension(name: String, noinline extension: R.() -> Any?) =
    registerExtension(R::class, name, extension)

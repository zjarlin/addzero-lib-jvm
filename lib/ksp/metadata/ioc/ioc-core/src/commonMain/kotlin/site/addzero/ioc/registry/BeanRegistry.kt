package site.addzero.ioc.registry

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
     * 根据名称获取 Bean 实例
     * @param name Bean 名称（函数名、类名等）
     * @return Bean 实例，如果不存在则返回 null
     */
    fun getBean(name: String): Any?

    /**
     * 根据名称获取 Bean 实例，如果不存在则抛出异常
     */
    fun getRequiredBean(name: String): Any {
        return getBean(name) ?: throw IllegalArgumentException("No bean found for name: $name")
    }

    /**
     * 根据名称获取指定类型的 Bean
     */
    fun <T : Any> getBean(name: String, clazz: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return getBean(name) as? T
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
     * 注册带名称的 Bean 提供者
     */
    fun <T : Any> registerProvider(name: String, clazz: KClass<T>, provider: () -> T)

    /**
     * 注册扩展函数 Bean
     * @param receiverClass receiver 类型
     * @param name 扩展函数名称
     * @param extension 扩展函数引用
     */
    fun <R : Any> registerExtension(receiverClass: KClass<R>, name: String, extension: R.() -> Any?)

    /**
     * 获取指定 receiver 类型的所有扩展函数
     */
    fun <R : Any> getExtensions(receiverClass: KClass<R>): Map<String, R.() -> Any?>

    /**
     * 获取指定 receiver 类型的指定名称的扩展函数
     */
    fun <R : Any> getExtension(receiverClass: KClass<R>, name: String): (R.() -> Any?)?

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

    // ============ 泛型 Bean 支持 ============

    /**
     * 根据 TypeKey 获取泛型 Bean
     */
    fun <T : Any> getBean(typeKey: TypeKey): T?

    /**
     * 根据 TypeKey 获取泛型 Bean，不存在则抛异常
     */
    fun <T : Any> getRequiredBean(typeKey: TypeKey): T {
        return getBean(typeKey) ?: throw IllegalArgumentException("No bean found for type: $typeKey")
    }

    /**
     * 注册泛型 Bean 实例
     */
    fun <T : Any> registerBean(typeKey: TypeKey, instance: T)

    /**
     * 注册泛型 Bean 提供者
     */
    fun <T : Any> registerProvider(typeKey: TypeKey, provider: () -> T)


}

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

/**
 * 按名称获取指定类型的 Bean
 */
inline fun <reified T : Any> BeanRegistry.getBean(name: String): T? = getBean(name, T::class)

/**
 * 按名称获取指定类型的 Bean，不存在则抛异常
 */
inline fun <reified T : Any> BeanRegistry.getRequiredBean(name: String): T =
    getBean(name, T::class) ?: throw IllegalArgumentException("No bean found for name: $name, type: ${T::class.simpleName}")

/**
 * 获取指定 receiver 类型的所有扩展函数
 */
inline fun <reified R : Any> BeanRegistry.getExtensions(): Map<String, R.() -> Any?> = getExtensions(R::class)

/**
 * 获取指定 receiver 类型的指定名称的扩展函数
 */
inline fun <reified R : Any> BeanRegistry.getExtension(name: String): (R.() -> Any?)? = getExtension(R::class, name)

/**
 * 在 receiver 上执行所有注册的扩展函数
 */
inline fun <reified R : Any> BeanRegistry.applyExtensions(receiver: R) {
    getExtensions<R>().values.forEach { ext -> receiver.ext() }
}

/**
 * 注册扩展函数
 */
inline fun <reified R : Any> BeanRegistry.registerExtension(name: String, noinline extension: R.() -> Any?) =
    registerExtension(R::class, name, extension)
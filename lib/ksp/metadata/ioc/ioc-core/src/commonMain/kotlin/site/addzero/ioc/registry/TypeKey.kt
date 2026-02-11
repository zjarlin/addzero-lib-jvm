package site.addzero.ioc.registry

import kotlin.reflect.KClass

/**
 * 泛型 Bean 的类型标识
 *
 * KClass 在运行时会擦除泛型，IService<User>::class 和 IService<Order>::class 是同一个 KClass。
 * TypeKey 通过 rawType + typeArgs 字符串来区分不同的泛型实例。
 *
 * 用法:
 *   val key = TypeKey.of<IService<User>>()
 *   registry.registerBean(key, UserServiceImpl())
 *   val service = registry.getBean<IService<User>>(key)
 */
data class TypeKey(
    val rawType: KClass<*>,
    val typeArgs: String
) {
    override fun toString(): String = "${rawType.simpleName}<$typeArgs>"

    companion object {
        /**
         * 手动构造 TypeKey
         *
         * 用法: TypeKey.of(IService::class, "User")
         */
        fun of(rawType: KClass<*>, vararg typeArgs: String): TypeKey {
            return TypeKey(rawType, typeArgs.joinToString(", "))
        }

        /**
         * 从 KClass + 泛型参数 KClass 构造
         *
         * 用法: TypeKey.of(IService::class, User::class)
         */
        fun of(rawType: KClass<*>, vararg typeArgs: KClass<*>): TypeKey {
            return TypeKey(rawType, typeArgs.joinToString(", ") { it.simpleName ?: "?" })
        }
    }
}

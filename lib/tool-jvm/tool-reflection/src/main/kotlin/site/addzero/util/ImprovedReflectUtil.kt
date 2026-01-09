@file:JvmName("ImprovedReflectUtils")
@file:Suppress("unused")

package site.addzero.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * 改进版反射工具类，提供带过期时间的缓存能力，避免 Hutool ReflectUtil 的内存泄漏问题。
 */
object ImprovedReflectUtil {

    private data class CacheEntry<V>(val value: V, val timestamp: Long = System.currentTimeMillis())

    private class ExpiringCache<V>(
        private val expireMillis: Long,
        private val maxSize: Int,
    ) {
        private val cache = ConcurrentHashMap<String, CacheEntry<V>>()
        private val lock = ReentrantLock()

        fun computeIfAbsent(key: String, supplier: () -> V): V {
            val existing = cache[key]
            if (existing != null && System.currentTimeMillis() - existing.timestamp < expireMillis) {
                return existing.value
            }
            val value = supplier()
            put(key, value)
            return value
        }

        private fun put(key: String, value: V) {
            lock.lock()
            try {
                if (cache.size >= maxSize) {
                    val victims = cache.entries
                        .sortedBy { it.value.timestamp }
                        .take(cache.size / 2)
                        .map { it.key }
                    victims.forEach { cache.remove(it) }
                }
                cache[key] = CacheEntry(value)
            } finally {
                lock.unlock()
            }
        }

        fun clear() {
            cache.clear()
        }

        fun cleanupExpired() {
            val now = System.currentTimeMillis()
            cache.entries.removeIf { now - it.value.timestamp >= expireMillis }
        }
    }

    private val constructorCache =
        ExpiringCache<Array<Constructor<*>>>(TimeUnit.MINUTES.toMillis(30), 1_000)
    private val fieldCache =
        ExpiringCache<Array<Field>>(TimeUnit.MINUTES.toMillis(30), 1_000)
    private val methodCache =
        ExpiringCache<Array<Method>>(TimeUnit.MINUTES.toMillis(30), 1_000)

    @Suppress("UNCHECKED_CAST")
    fun <T> getConstructor(clazz: Class<T>?, vararg parameterTypes: Class<*>): Constructor<T>? {
        clazz ?: return null
        return getConstructors(clazz).firstOrNull { constructor ->
            val params = constructor.parameterTypes
            params.size == parameterTypes.size &&
                params.zip(parameterTypes).all { (src, dest) -> src.isAssignableFrom(dest) }
        }?.also { it.isAccessible = true } as Constructor<T>?
    }

    fun getConstructors(clazz: Class<*>): Array<Constructor<*>> =
        constructorCache.computeIfAbsent(clazz.name) {
            clazz.declaredConstructors.also { constructors ->
                constructors.forEach { it.isAccessible = true }
            }
        }

    fun getFields(clazz: Class<*>): Array<Field> =
        fieldCache.computeIfAbsent(clazz.name) {
            val fields = mutableListOf<Field>()
            var current: Class<*>? = clazz
            while (current != null && current != Any::class.java) {
                fields += current.declaredFields
                current = current.superclass
            }
            fields.onEach { it.isAccessible = true }.toTypedArray()
        }

    fun getMethods(clazz: Class<*>): Array<Method> =
        methodCache.computeIfAbsent(clazz.name) {
            val methods = mutableListOf<Method>()
            var current: Class<*>? = clazz
            while (current != null && current != Any::class.java) {
                methods += current.declaredMethods
                current = current.superclass
            }
            methods.onEach { it.isAccessible = true }.toTypedArray()
        }

    fun getFieldValue(target: Any, field: Field): Any? =
        runCatching {
            field.isAccessible = true
            field.get(target)
        }.getOrNull()

    fun getFieldValue(target: Any, fieldName: String): Any? =
        runCatching {
            val field = target.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(target)
        }.getOrNull()

    fun setFieldValue(target: Any, field: Field, value: Any?) {
        runCatching {
            field.isAccessible = true
            field.set(target, value)
        }
    }

    fun setFieldValue(target: Any, fieldName: String, value: Any?) {
        val field = runCatching { target.javaClass.getDeclaredField(fieldName) }.getOrNull() ?: return
        setFieldValue(target, field, value)
    }

    fun invoke(target: Any, method: Method, vararg args: Any?): Any? =
        runCatching {
            method.isAccessible = true
            method.invoke(target, *args)
        }.getOrNull()

    fun newInstance(clazz: Class<*>, vararg args: Any?): Any? =
        runCatching {
            if (args.isEmpty()) {
                clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
            } else {
                val signature = args.map { it?.javaClass ?: Any::class.java }.toTypedArray()
                clazz.getDeclaredConstructor(*signature).apply { isAccessible = true }.newInstance(*args)
            }
        }.getOrNull()

    fun clearAllCaches() {
        constructorCache.clear()
        fieldCache.clear()
        methodCache.clear()
    }

    fun cleanupExpiredEntries() {
        constructorCache.cleanupExpired()
        fieldCache.cleanupExpired()
        methodCache.cleanupExpired()
    }

}

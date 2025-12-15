package site.addzero.aop.dicttrans.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * 反射工具类，避免使用Class作为缓存key防止内存泄漏
 *
 * @author zjarlin
 */
@Deprecated("改用ImprovedReflectUtil")
internal object ReflectUtil {

//
//    /**
//     * 构造函数缓存，带过期时间和最大容量限制
//     */
//    private val CONSTRUCTORS_CACHE = ExpiringCache < Class<*>, Constructor<*>[]>(TimeUnit.MINUTES.toMillis(30), 1000)
//
//    /**
//     * 字段缓存，带过期时间和最大容量限制
//     */
//    private val FIELDS_CACHE = ExpiringCache < Class<*>, Field[]>(TimeUnit.MINUTES.toMillis(30), 1000)
//
//    /**
//     * 方法缓存，带过期时间和最大容量限制
//     */
//    private val METHODS_CACHE = ExpiringCache < Class<*>, Method[]>(TimeUnit.MINUTES.toMillis(30), 1000)

    /**
     * 获取类的所有字段（包括父类）
     */
    fun getFields(clazz: Class<*>): Array<Field> {
        val className = clazz.name
        return FIELDS_CACHE.computeIfAbsent(className) {
            getAllFields(clazz).toTypedArray()
        }
    }

    /**
     * 获取类的所有字段（包括父类），带过滤器
     */
    fun getFields(clazz: Class<*>, filter: (Field) -> Boolean): Array<Field> {
        return getFields(clazz).filter(filter).toTypedArray()
    }

    /**
     * 递归获取所有字段（包括父类）
     */
    private fun getAllFields(clazz: Class<*>): List<Field> {
        val fields = mutableListOf<Field>()
        var currentClass: Class<*>? = clazz

        while (currentClass != null && currentClass != Any::class.java) {
            fields.addAll(currentClass.declaredFields)
            currentClass = currentClass.superclass
        }

        return fields
    }

    /**
     * 获取字段值
     */
    fun getFieldValue(obj: Any, field: Field): Any? {
        return try {
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 根据字段名获取字段值
     */
    fun getFieldValue(obj: Any, fieldName: String): Any? {
        return try {
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 设置字段值
     */
    fun setFieldValue(obj: Any, field: Field, value: Any?) {
        try {
            field.isAccessible = true
            field.set(obj, value)
        } catch (e: Exception) {
            // 忽略设置失败的情况
        }
    }

    /**
     * 根据字段名设置字段值
     */
    fun setFieldValue(obj: Any, fieldName: String, value: Any?) {
        try {
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(obj, value)
        } catch (e: Exception) {
            // 忽略设置失败的情况
        }
    }

    /**
     * 获取类的所有方法
     */
    fun getMethods(clazz: Class<*>): Array<Method> {
        val className = clazz.name
        return METHODS_CACHE.computeIfAbsent(className) {
            clazz.methods
        }
    }

    /**
     * 获取类的所有构造器
     */
    fun getConstructors(clazz: Class<*>): Array<Constructor<*>> {
        val className = clazz.name
        return CONSTRUCTORS_CACHE.computeIfAbsent(className) {
            clazz.constructors
        }
    }

    /**
     * 调用方法
     */
    fun invoke(obj: Any, method: Method, vararg args: Any?): Any? {
        return try {
            method.isAccessible = true
            method.invoke(obj, *args)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 创建实例
     */
    fun newInstance(clazz: Class<*>, vararg args: Any?): Any? {
        return try {
            if (args.isEmpty()) {
                clazz.newInstance()
            } else {
                val constructor = clazz.getConstructor(*args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
                constructor.newInstance(*args)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 清理缓存（可选，用于内存管理）
     */
    fun clearCache() {
        CONSTRUCTORS_CACHE.clear()
        FIELDS_CACHE.clear()
        METHODS_CACHE.clear()
    }

    /**
     * 带过期时间和最大容量的缓存实现
     */
    private class ExpiringCache<K, V>(
        private val expireTimeMillis: Long,
        private val maxSize: Int
    ) {
        private val cache = ConcurrentHashMap<K, CacheEntry<V>>()
        private val lock = ReentrantLock()

        /**
         * 如果键不存在，则计算值并放入缓存
         */
        fun computeIfAbsent(key: K, mappingFunction: (K) -> V): V {
            val entry = cache[key]
            if (entry != null && System.currentTimeMillis() - entry.createTime < expireTimeMillis) {
                return entry.value
            }

            val newValue = mappingFunction(key)
            put(key, newValue)
            return newValue
        }

        /**
         * 放入缓存，如果超过最大容量则移除最旧的条目
         */
        private fun put(key: K, value: V) {
            lock.lock()
            try {
                if (cache.size >= maxSize) {
                    // 移除最旧的一半条目
                    val entriesToRemove = cache.entries
                        .sortedBy { it.value.createTime }
                        .take(cache.size / 2)
                        .map { it.key }

                    entriesToRemove.forEach { cache.remove(it) }
                }

                cache[key] = CacheEntry(value)
            } finally {
                lock.unlock()
            }
        }
    }
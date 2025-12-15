@file:JvmName("ImprovedReflectUtils")
@file:Suppress("unused")

package site.addzero.util

import java.lang.reflect.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * 改进版反射工具类
 *
 * 解决Hutool ReflectUtil中存在的内存泄漏问题，通过以下方式：
 * 1. 使用带过期时间的缓存机制
 * 2. 提供主动清理缓存的方法
 * 3. 控制缓存大小上限
 *
 * @author zjarlin
 * @since 2025/12/15
 */
object ImprovedReflectUtil {
    
    /**
     * 缓存条目包装类，包含值和创建时间
     */
    private class CacheEntry<T>(
        val value: T,
        val createTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 构造函数缓存，带过期时间和最大容量限制
     */
    private val CONSTRUCTORS_CACHE = ExpiringCache<Class<*>, Constructor<*>[]>(TimeUnit.MINUTES.toMillis(30), 1000)
    
    /**
     * 字段缓存，带过期时间和最大容量限制
     */
    private val FIELDS_CACHE = ExpiringCache<Class<*>, Field[]>(TimeUnit.MINUTES.toMillis(30), 1000)
    
    /**
     * 方法缓存，带过期时间和最大容量限制
     */
    private val METHODS_CACHE = ExpiringCache<Class<*>, Method[]>(TimeUnit.MINUTES.toMillis(30), 1000)
    
    /**
     * 查找类中的指定参数的构造方法
     *
     * @param <T> 对象类型
     * @param clazz 类
     * @param parameterTypes 参数类型
     * @return 构造方法，如果未找到返回null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getConstructor(clazz: Class<T>?, vararg parameterTypes: Class<*>): Constructor<T>? {
        if (clazz == null) {
            return null
        }
        
        val constructors = getConstructors(clazz)
        for (constructor in constructors) {
            val pts = constructor.parameterTypes
            if (isAllAssignableFrom(pts, parameterTypes)) {
                setAccessible(constructor)
                return constructor as Constructor<T>
            }
        }
        return null
    }
    
    /**
     * 获取类的所有构造方法
     *
     * @param clazz 类
     * @return 构造方法数组
     */
    fun getConstructors(clazz: Class<*>): Array<Constructor<*>> {
        return CONSTRUCTORS_CACHE.computeIfAbsent(clazz) {
            it.declaredConstructors.apply {
                for (constructor in this) {
                    setAccessible(constructor)
                }
            }
        }
    }
    
    /**
     * 获取类的所有字段（包括父类）
     *
     * @param clazz 类
     * @return 字段数组
     */
    fun getFields(clazz: Class<*>): Array<Field> {
        return FIELDS_CACHE.computeIfAbsent(clazz) {
            getAllFields(it)
        }
    }
    
    /**
     * 获取类的所有方法（包括父类）
     *
     * @param clazz 类
     * @return 方法数组
     */
    fun getMethods(clazz: Class<*>): Array<Method> {
        return METHODS_CACHE.computeIfAbsent(clazz) {
            getAllMethods(it)
        }
    }
    
    /**
     * 获取类的所有字段（包括父类）
     *
     * @param clazz 类
     * @return 字段数组
     */
    private fun getAllFields(clazz: Class<*>): Array<Field> {
        val fields = mutableListOf<Field>()
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            fields.addAll(currentClass.declaredFields)
            currentClass = currentClass.superclass
        }
        return fields.toTypedArray()
    }
    
    /**
     * 获取类的所有方法（包括父类）
     *
     * @param clazz 类
     * @return 方法数组
     */
    private fun getAllMethods(clazz: Class<*>): Array<Method> {
        val methods = mutableListOf<Method>()
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            methods.addAll(currentClass.declaredMethods)
            currentClass = currentClass.superclass
        }
        return methods.toTypedArray()
    }
    
    /**
     * 检查所有给定的参数类型是否都可以从源类型数组中分配
     *
     * @param srcTypes 源类型数组
     * @param destTypes 目标类型数组
     * @return 是否可以分配
     */
    private fun isAllAssignableFrom(srcTypes: Array<Class<*>>, destTypes: Array<out Class<*>>): Boolean {
        if (srcTypes.size != destTypes.size) {
            return false
        }
        
        for (i in srcTypes.indices) {
            if (!srcTypes[i].isAssignableFrom(destTypes[i])) {
                return false
            }
        }
        return true
    }
    
    /**
     * 设置可访问性
     *
     * @param accessibleObject 可访问对象
     */
    private fun setAccessible(accessibleObject: AccessibleObject) {
        if (accessibleObject !is Field || !Modifier.isPublic(accessibleObject.modifiers) || !Modifier.isPublic(accessibleObject.declaringClass.modifiers)) {
            accessibleObject.isAccessible = true
        }
    }
    
    /**
     * 清理所有缓存
     */
    fun clearAllCaches() {
        CONSTRUCTORS_CACHE.clear()
        FIELDS_CACHE.clear()
        METHODS_CACHE.clear()
    }
    
    /**
     * 清理过期的缓存条目
     */
    fun cleanupExpiredEntries() {
        CONSTRUCTORS_CACHE.cleanupExpired()
        FIELDS_CACHE.cleanupExpired()
        METHODS_CACHE.cleanupExpired()
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
        
        /**
         * 清理过期的条目
         */
        fun cleanupExpired() {
            val currentTime = System.currentTimeMillis()
            val keysToRemove = mutableListOf<K>()
            
            cache.forEach { (key, entry) ->
                if (currentTime - entry.createTime >= expireTimeMillis) {
                    keysToRemove.add(key)
                }
            }
            
            keysToRemove.forEach { cache.remove(it) }
        }
        
        /**
         * 清空缓存
         */
        fun clear() {
            cache.clear()
        }
    }
}
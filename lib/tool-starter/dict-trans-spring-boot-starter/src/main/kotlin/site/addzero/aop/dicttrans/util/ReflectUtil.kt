package site.addzero.aop.dicttrans.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 反射工具类，避免使用Class作为缓存key防止内存泄漏
 * 
 * @author zjarlin
 */
internal object ReflectUtil {
    
    /**
     * 构造对象缓存 - 使用类名作为key
     */
    private val CONSTRUCTORS_CACHE = ConcurrentHashMap<String, Array<Constructor<*>>>()
    
    /**
     * 字段缓存 - 使用类名作为key
     */
    private val FIELDS_CACHE = ConcurrentHashMap<String, Array<Field>>()
    
    /**
     * 方法缓存 - 使用类名作为key
     */
    private val METHODS_CACHE = ConcurrentHashMap<String, Array<Method>>()
    
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
}
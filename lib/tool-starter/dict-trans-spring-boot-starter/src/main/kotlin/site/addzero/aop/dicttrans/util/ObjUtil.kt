package site.addzero.aop.dicttrans.util

/**
 * 对象工具类，替代hutool的ObjUtil
 * 
 * @author zjarlin
 */
object ObjUtil {
    
    /**
     * 判断对象是否为null
     */
    fun isNull(obj: Any?): Boolean {
        return obj == null
    }
    
    /**
     * 判断对象是否不为null
     */
    fun isNotNull(obj: Any?): Boolean {
        return obj != null
    }
    
    /**
     * 如果对象为null则返回默认值
     */
    fun <T> defaultIfNull(obj: T?, defaultValue: T): T {
        return obj ?: defaultValue
    }
    
    /**
     * 判断对象是否为空（null或空字符串）
     */
    fun isEmpty(obj: Any?): Boolean {
        return when (obj) {
            null -> true
            is String -> obj.isEmpty()
            is Collection<*> -> obj.isEmpty()
            is Map<*, *> -> obj.isEmpty()
            is Array<*> -> obj.isEmpty()
            else -> false
        }
    }
    
    /**
     * 判断对象是否不为空
     */
    fun isNotEmpty(obj: Any?): Boolean {
        return !isEmpty(obj)
    }
}
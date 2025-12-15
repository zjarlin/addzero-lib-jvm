package site.addzero.aop.dicttrans.util

/**
 * Map工具类，替代hutool的MapUtil
 * 
 * @author zjarlin
 */
object MapUtil {
    
    /**
     * 判断Map是否为空
     */
    fun isEmpty(map: Map<*, *>?): Boolean {
        return map == null || map.isEmpty()
    }
    
    /**
     * 判断Map是否不为空
     */
    fun isNotEmpty(map: Map<*, *>?): Boolean {
        return !isEmpty(map)
    }
    
    /**
     * 创建新的HashMap
     */
    fun <K, V> newHashMap(): MutableMap<K, V> {
        return mutableMapOf()
    }
    
    /**
     * 创建新的HashMap并添加键值对
     */
    fun <K, V> newHashMap(vararg pairs: Pair<K, V>): MutableMap<K, V> {
        return mutableMapOf(*pairs)
    }
    
    /**
     * 获取Map的大小
     */
    fun size(map: Map<*, *>?): Int {
        return map?.size ?: 0
    }
}
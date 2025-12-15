package site.addzero.aop.dicttrans.util

/**
 * 集合工具类，替代hutool的CollUtil
 * 
 * @author zjarlin
 */
internal object CollUtil {
    
    /**
     * 判断集合是否为空
     */
    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }
    
    /**
     * 判断集合是否不为空
     */
    fun isNotEmpty(collection: Collection<*>?): Boolean {
        return !isEmpty(collection)
    }
    
    /**
     * 判断数组是否为空
     */
    fun isEmpty(array: Array<*>?): Boolean {
        return array == null || array.isEmpty()
    }
    
    /**
     * 判断数组是否不为空
     */
    fun isNotEmpty(array: Array<*>?): Boolean {
        return !isEmpty(array)
    }
    
    /**
     * 获取集合大小
     */
    fun size(collection: Collection<*>?): Int {
        return collection?.size ?: 0
    }
    
    /**
     * 创建新的ArrayList
     */
    fun <T> newArrayList(): MutableList<T> {
        return mutableListOf()
    }
    
    /**
     * 创建新的ArrayList并添加元素
     */
    fun <T> newArrayList(vararg elements: T): MutableList<T> {
        return mutableListOf(*elements)
    }
}
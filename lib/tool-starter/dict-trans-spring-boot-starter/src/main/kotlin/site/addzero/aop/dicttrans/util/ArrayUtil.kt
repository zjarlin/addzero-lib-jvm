package site.addzero.aop.dicttrans.util

/**
 * 数组工具类，替代hutool的ArrayUtil
 *
 * @author zjarlin
 */
internal object ArrayUtil {

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
     * 获取数组长度
     */
    fun length(array: Array<*>?): Int {
        return array?.size ?: 0
    }

    /**
     * 判断数组中是否包含指定元素
     */
    fun <T> contains(array: Array<T>?, element: T): Boolean {
        return array?.contains(element) ?: false
    }
}

package site.addzero.aop.dicttrans.util

/**
 * 字符序列工具类，替代hutool的CharSequenceUtil
 * 
 * @author zjarlin
 */
internal object CharSequenceUtil {
    
    /**
     * 判断字符序列是否为空白
     */
    fun isBlank(cs: CharSequence?): Boolean {
        return cs == null || cs.toString().trim().isEmpty()
    }
    
    /**
     * 判断字符序列是否不为空白
     */
    fun isNotBlank(cs: CharSequence?): Boolean {
        return !isBlank(cs)
    }
    
    /**
     * 判断字符序列是否为空
     */
    fun isEmpty(cs: CharSequence?): Boolean {
        return cs == null || cs.isEmpty()
    }
    
    /**
     * 判断字符序列是否不为空
     */
    fun isNotEmpty(cs: CharSequence?): Boolean {
        return !isEmpty(cs)
    }
    
    /**
     * 去除首尾空白字符
     */
    fun trim(cs: CharSequence?): String {
        return cs?.toString()?.trim() ?: ""
    }
    
    /**
     * 判断是否包含指定字符序列
     */
    fun contains(cs: CharSequence?, searchCs: CharSequence?): Boolean {
        return cs != null && searchCs != null && cs.toString().contains(searchCs.toString())
    }
    
    /**
     * 返回第一个非空白字符序列
     */
    fun firstNonBlank(vararg css: CharSequence?): String? {
        return css.firstOrNull { isNotBlank(it) }?.toString()
    }
}
package site.addzero.aop.dicttrans.util

/**
 * 字符串工具类，替代hutool的StrUtil
 * 
 * @author zjarlin
 */
object StrUtil {
    
    /**
     * 判断字符串是否为空白
     */
    fun isBlank(str: CharSequence?): Boolean {
        return str == null || str.toString().trim().isEmpty()
    }
    
    /**
     * 判断字符串是否不为空白
     */
    fun isNotBlank(str: CharSequence?): Boolean {
        return !isBlank(str)
    }
    
    /**
     * 判断所有字符串是否都为空白
     */
    fun isAllBlank(vararg strs: CharSequence?): Boolean {
        return strs.all { isBlank(it) }
    }
    
    /**
     * 判断是否包含指定字符串
     */
    fun contains(str: CharSequence?, searchStr: CharSequence?): Boolean {
        return str != null && searchStr != null && str.toString().contains(searchStr.toString())
    }
    
    /**
     * 转换为下划线命名
     */
    fun toUnderlineCase(str: String?): String {
        if (str == null) return ""
        
        val result = StringBuilder()
        var prevChar: Char? = null
        
        for (char in str) {
            if (char.isUpperCase() && prevChar != null && prevChar.isLowerCase()) {
                result.append('_')
            }
            result.append(char.lowercaseChar())
            prevChar = char
        }
        
        return result.toString()
    }
    
    /**
     * 如果字符串为空则返回默认值
     */
    fun ifBlank(str: String?, defaultValue: String = ""): String {
        return if (isBlank(str)) defaultValue else str!!
    }
    
    /**
     * 判断字符串是否为空
     */
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.isEmpty()
    }
    
    /**
     * 判断字符串是否不为空
     */
    fun isNotEmpty(str: CharSequence?): Boolean {
        return !isEmpty(str)
    }
    
    /**
     * 转换为驼峰命名
     */
    fun toCamelCase(str: String?): String {
        if (str == null) return ""
        
        val result = StringBuilder()
        var capitalizeNext = false
        
        for (char in str) {
            when {
                char == '_' || char == '-' -> capitalizeNext = true
                capitalizeNext -> {
                    result.append(char.uppercaseChar())
                    capitalizeNext = false
                }
                else -> result.append(char.lowercaseChar())
            }
        }
        
        return result.toString()
    }
    
    /**
     * 判断是否包含任意一个字符串
     */
    fun containsAny(str: CharSequence?, vararg searchStrs: CharSequence?): Boolean {
        if (str == null) return false
        return searchStrs.any { searchStr -> 
            searchStr != null && str.toString().contains(searchStr.toString())
        }
    }
    
    /**
     * 分割字符串
     */
    fun split(str: String?, delimiter: String): List<String> {
        return str?.split(delimiter) ?: emptyList()
    }
    
    /**
     * 返回第一个非空白字符串
     */
    fun firstNonBlank(vararg strs: String?): String? {
        return strs.firstOrNull { isNotBlank(it) }
    }
}
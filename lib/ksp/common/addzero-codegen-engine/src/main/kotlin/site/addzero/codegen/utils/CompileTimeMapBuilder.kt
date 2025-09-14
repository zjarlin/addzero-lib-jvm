package site.addzero.codegen.utils

import java.util.Date

/**
 * 编译时友好的Map构建器
 *
 * 专为KSP编译时代码生成设计，不依赖反射或序列化
 */
class CompileTimeMapBuilder {
    private val map = mutableMapOf<String, Any>()

    /**
     * 添加键值对
     */
    infix fun String.to(value: Any?) {
        if (value != null) {
            map[this] = value
        }
    }

    /**
     * 添加字符串值
     */
    fun put(key: String, value: String) {
        map[key] = value
    }

    /**
     * 添加数字值
     */
    fun put(key: String, value: Number) {
        map[key] = value
    }

    /**
     * 添加布尔值
     */
    fun put(key: String, value: Boolean) {
        map[key] = value
    }

    /**
     * 添加列表
     */
    fun put(key: String, value: List<*>) {
        map[key] = value
    }

    /**
     * 添加Map
     */
    fun put(key: String, value: Map<*, *>) {
        map[key] = value
    }

    /**
     * 条件性添加
     */
    fun putIf(condition: Boolean, key: String, value: Any?) {
        if (condition && value != null) {
            map[key] = value
        }
    }

    /**
     * 计算属性
     */
    fun computed(key: String, block: () -> Any?) {
        val value = block()
        if (value != null) {
            map[key] = value
        }
    }

    /**
     * 添加通用的生成信息
     */
    fun addGenerationInfo() {
        computed("timestamp") { System.currentTimeMillis() }
        computed("generatedBy") { "AddZero Code Generator" }
        computed("generatedDate") {
            // 使用简单的日期格式，避免依赖java.time
            Date().toString()
        }
    }

    /**
     * 构建最终的Map
     */
    fun build(): Map<String, Any> = map.toMap()
}

/**
 * DSL函数
 */
inline fun buildContext(block: CompileTimeMapBuilder.() -> Unit): Map<String, Any> {
    return CompileTimeMapBuilder().apply(block).build()
}

/**
 * 属性转换器接口
 * 用于将复杂对象转换为简单的Map结构
 */
interface PropertyConverter<T> {
    fun convert(source: T): Map<String, Any>
}

/**
 * 列表转换器
 */
object ListConverter {

    /**
     * 转换对象列表为Map列表
     */
    inline fun <T> convertList(
        source: List<T>,
        converter: (T) -> Map<String, Any>
    ): List<Map<String, Any>> {
        return source.map(converter)
    }

    /**
     * 转换字符串列表
     */
    fun convertStringList(source: List<String>): List<String> = source

    /**
     * 过滤并转换列表
     */
    inline fun <T> convertListIf(
        source: List<T>,
        predicate: (T) -> Boolean,
        converter: (T) -> Map<String, Any>
    ): List<Map<String, Any>> {
        return source.filter(predicate).map(converter)
    }
}

/**
 * 通用的字段转换工具
 */
object FieldConverter {

    /**
     * 转换为驼峰命名
     */
    fun toCamelCase(str: String): String {
        return str.split("_", "-", " ")
            .mapIndexed { index, word ->
                if (index == 0) word.lowercase()
                else word.lowercase().replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
    }

    /**
     * 转换为帕斯卡命名
     */
    fun toPascalCase(str: String): String {
        return str.split("_", "-", " ")
            .joinToString("") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    /**
     * 转换为下划线命名
     */
    fun toSnakeCase(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    }

    /**
     * 安全的字符串转换
     */
    fun safeString(value: Any?): String {
        return value?.toString() ?: ""
    }

    /**
     * 检查字符串是否非空
     */
    fun isNotEmpty(str: String?): Boolean {
        return !str.isNullOrEmpty()
    }

    /**
     * 获取文件名（不含扩展名）
     */
    fun getFileNameWithoutExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(0, lastDot) else fileName
    }
}

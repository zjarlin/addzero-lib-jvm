package site.addzero.example

/**
 * 输入类型接口 - 模拟 Jimmer 的 Input 类型
 */
interface Input<E> {
    fun toEntity(): E
}

/**
 * Draft 类型接口 - 模拟 Jimmer 的 Draft 类型
 */
interface Draft<E> {
    fun toEntity(): E
}

/**
 * 字符串输入实现
 */
class StringInput(private val value: String) : Input<String> {
    override fun toEntity(): String = value
}

/**
 * 字符串 Draft 实现
 */
class StringDraft(private val value: String) : Draft<String> {
    override fun toEntity(): String = value
}

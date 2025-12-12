package site.addzero.apt.dict.annotations

import kotlin.reflect.KClass

/**
 * 编译时字典翻译注解
 * 
 * 使用示例：
 * ```kotlin
 * @DictTranslate
 * data class UserVO(
 *     val id: Long,
 *     @DictField(dictCode = "user_status", targetField = "statusName")
 *     val status: String,
 *     @DictField(table = "sys_dept", codeColumn = "id", nameColumn = "name", targetField = "deptName")
 *     val deptId: Long
 * )
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DictTranslate(
    /**
     * 生成的类名后缀，默认为 "Enhanced"
     */
    val suffix: String = "Enhanced",
    
    /**
     * 是否生成扩展函数，默认为 true
     */
    val generateExtensions: Boolean = true,
    
    /**
     * 是否生成 Builder 模式，默认为 false
     */
    val generateBuilder: Boolean = false
)
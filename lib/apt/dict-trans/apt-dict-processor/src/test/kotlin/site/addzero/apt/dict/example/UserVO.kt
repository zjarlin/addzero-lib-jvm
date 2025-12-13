package site.addzero.apt.dict.example

import site.addzero.apt.dict.annotations.DictTranslate
import site.addzero.apt.dict.annotations.DictField

/**
 * 用户 VO 示例
 * 
 * 编译后将生成 UserVOEnhanced 类，包含翻译后的字段
 */
@DictTranslate(suffix = "Enhanced", generateExtensions = true)
data class UserVO(
    val id: Long,
    val name: String,
    
    @DictField(dictCode = "user_status", targetField = "statusName")
    val status: String,
    
    @DictField(dictCode = "user_type", targetField = "typeName")
    val type: String,
    
    @DictField(
        table = "sys_dept", 
        codeColumn = "id", 
        nameColumn = "name", 
        targetField = "deptName"
    )
    val deptId: Long,
    
    @DictField(
        table = "sys_role",
        codeColumn = "id",
        nameColumn = "role_name",
        targetField = "roleName"
    )
    val roleId: Long?
)

/**
 * 使用示例：
 * 
 * ```kotlin
 * // 原始对象
 * val user = UserVO(
 *     id = 1L,
 *     name = "张三",
 *     status = "1",
 *     type = "admin",
 *     deptId = 100L,
 *     roleId = 1L
 * )
 * 
 * // 转换为增强对象
 * val enhancedUser = user.toEnhanced()
 * 
 * // 执行字典翻译
 * enhancedUser.translate(transApi)
 * 
 * // 现在可以访问翻译后的字段
 * println(enhancedUser.statusName) // "正常"
 * println(enhancedUser.typeName)   // "管理员"
 * println(enhancedUser.deptName)   // "技术部"
 * println(enhancedUser.roleName)   // "系统管理员"
 * ```
 */
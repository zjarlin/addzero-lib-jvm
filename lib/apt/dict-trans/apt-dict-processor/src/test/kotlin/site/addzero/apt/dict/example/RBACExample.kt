package site.addzero.apt.dict.example

import site.addzero.apt.dict.annotations.DictField
import site.addzero.apt.dict.annotations.DictTranslate

/**
 * RBAC (Role-Based Access Control) 示例
 * 
 * 这个示例展示了如何使用编译时字典翻译来处理复杂的RBAC场景，
 * 包括系统字典、表字典和嵌套结构的翻译。
 * 
 * 使用方式：
 * ```kotlin
 * val user = UserEntity(
 *     id = 1L,
 *     username = "admin",
 *     status = 1,
 *     departmentId = 100L,
 *     roles = listOf(
 *         RoleEntity(id = 1L, roleCode = "ADMIN", status = 1),
 *         RoleEntity(id = 2L, roleCode = "USER", status = 1)
 *     ),
 *     permissions = listOf(
 *         PermissionEntity(id = 1L, permissionCode = "USER_READ", resourceType = "USER"),
 *         PermissionEntity(id = 2L, permissionCode = "USER_WRITE", resourceType = "USER")
 *     )
 * )
 * 
 * // 转换为增强对象并执行批量翻译
 * val enhancedUser = user.toEnhanced()
 * enhancedUser.translate(transApi)
 * 
 * // 访问翻译后的字段
 * println("用户状态: ${enhancedUser.statusText}")
 * println("部门名称: ${enhancedUser.departmentName}")
 * println("角色列表: ${enhancedUser.rolesEnhanced?.map { it.roleText }}")
 * println("权限列表: ${enhancedUser.permissionsEnhanced?.map { it.permissionText }}")
 * ```
 */

/**
 * 用户实体 - 展示系统字典和表字典翻译
 */
@DictTranslate
data class UserEntity(
    val id: Long,
    val username: String,
    
    // 系统字典翻译 - 用户状态
    @DictField(dictCode = "user_status")
    val status: Int,
    
    // 表字典翻译 - 部门信息
    @DictField(
        table = "sys_department", 
        codeColumn = "id", 
        nameColumn = "dept_name"
    )
    val departmentId: Long,
    
    // 嵌套结构翻译 - 角色列表
    val roles: List<RoleEntity>? = null,
    
    // 嵌套结构翻译 - 权限列表
    val permissions: List<PermissionEntity>? = null
)

/**
 * 角色实体 - 展示系统字典和多重注解
 */
@DictTranslate
data class RoleEntity(
    val id: Long,
    
    // 表字典翻译 - 角色名称
    @DictField(
        table = "sys_role",
        codeColumn = "role_code", 
        nameColumn = "role_name"
    )
    val roleCode: String,
    
    // 系统字典翻译 - 角色状态
    @DictField(dictCode = "role_status")
    val status: Int,
    
    // 多重字典翻译 - 角色类型（支持多个@DictField注解）
    @DictField(dictCode = "role_type")
    @DictField(
        table = "sys_role_type",
        codeColumn = "type_code",
        nameColumn = "type_desc"
    )
    val roleType: String? = null
)

/**
 * 权限实体 - 展示表字典和条件翻译
 */
@DictTranslate  
data class PermissionEntity(
    val id: Long,
    
    // 表字典翻译 - 权限名称
    @DictField(
        table = "sys_permission",
        codeColumn = "permission_code",
        nameColumn = "permission_name"
    )
    val permissionCode: String,
    
    // 条件表字典翻译 - 资源类型（带WHERE条件）
    @DictField(
        table = "sys_resource_type",
        codeColumn = "type_code", 
        nameColumn = "type_name",
        condition = "status = 1 AND is_active = true"
    )
    val resourceType: String,
    
    // 系统字典翻译 - 权限状态
    @DictField(dictCode = "permission_status")
    val status: Int? = 1
)

/**
 * 组织架构实体 - 展示递归嵌套翻译
 */
@DictTranslate
data class OrganizationEntity(
    val id: Long,
    val orgCode: String,
    
    // 表字典翻译 - 组织名称
    @DictField(
        table = "sys_organization",
        codeColumn = "org_code",
        nameColumn = "org_name"
    )
    val orgName: String,
    
    // 系统字典翻译 - 组织类型
    @DictField(dictCode = "org_type")
    val orgType: String,
    
    // 递归嵌套翻译 - 父级组织
    val parentOrg: OrganizationEntity? = null,
    
    // 递归嵌套翻译 - 子级组织列表
    val childOrgs: List<OrganizationEntity>? = null
)

/**
 * 复杂业务场景 - 展示SPEL表达式和动态翻译
 */
@DictTranslate
data class BusinessEntity(
    val id: Long,
    val businessCode: String,
    
    // SPEL表达式翻译 - 动态状态文本
    @DictField(
        spelExp = "status == 1 ? '正常' : (status == 2 ? '暂停' : '禁用')"
    )
    val status: Int,
    
    // SPEL表达式翻译 - 基于多个字段的复合翻译
    @DictField(
        spelExp = "level + '-' + category + '-' + region"
    )
    val level: String,
    val category: String,
    val region: String,
    
    // 条件表字典翻译 - 基于当前对象属性的WHERE条件
    @DictField(
        table = "business_config",
        codeColumn = "config_key",
        nameColumn = "config_value", 
        condition = "business_type = #{businessType} AND region = #{region}"
    )
    val businessType: String,
    
    // 嵌套复杂对象
    val userInfo: UserEntity? = null
)

/**
 * DSL模板示例 - 展示如何定义翻译规则
 * 
 * 这个类展示了DSL的设计思路，实际的DSL语法会在模板文件中定义
 */
class RBACTranslationDSL {
    
    /**
     * 用户翻译规则
     */
    fun userTranslationRules() = """
        entity UserEntity {
            field status -> systemDict("user_status")
            field departmentId -> tableDict("sys_department", "id", "dept_name")
            
            nested roles -> collection<RoleEntity> {
                field roleCode -> tableDict("sys_role", "role_code", "role_name")
                field status -> systemDict("role_status")
                field roleType -> [
                    systemDict("role_type"),
                    tableDict("sys_role_type", "type_code", "type_desc")
                ]
            }
            
            nested permissions -> collection<PermissionEntity> {
                field permissionCode -> tableDict("sys_permission", "permission_code", "permission_name")
                field resourceType -> tableDict("sys_resource_type", "type_code", "type_name") {
                    where "status = 1 AND is_active = true"
                }
                field status -> systemDict("permission_status")
            }
        }
    """.trimIndent()
    
    /**
     * 组织架构递归翻译规则
     */
    fun organizationTranslationRules() = """
        entity OrganizationEntity {
            field orgName -> tableDict("sys_organization", "org_code", "org_name")
            field orgType -> systemDict("org_type")
            
            recursive parentOrg -> OrganizationEntity
            recursive childOrgs -> collection<OrganizationEntity>
        }
    """.trimIndent()
    
    /**
     * 业务实体SPEL翻译规则
     */
    fun businessTranslationRules() = """
        entity BusinessEntity {
            field status -> spel("status == 1 ? '正常' : (status == 2 ? '暂停' : '禁用')")
            field level -> spel("level + '-' + category + '-' + region")
            field businessType -> tableDict("business_config", "config_key", "config_value") {
                where "business_type = #{businessType} AND region = #{region}"
            }
            
            nested userInfo -> UserEntity
        }
    """.trimIndent()
}
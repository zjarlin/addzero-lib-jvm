package site.addzero.system.spi

import site.addzero.system.spi.rbac.dto.UserRoleDTO

/**
 * 用户角色绑定服务SPI
 */
interface UserRoleSpi {
    /**
     * 为用户分配角色
     */
    fun assignRoles(userId: String, roleIds: Set<String>)

    /**
     * 获取用户的角色ID列表
     */
    fun getRoleIds(userId: String): Set<String>

    /**
     * 获取用户的角色详情列表
     */
    fun getRoles(userId: String): List<UserRoleDTO>

    /**
     * 移除用户的某个角色
     */
    fun removeRole(userId: String, roleId: String)

    /**
     * 清空用户所有角色
     */
    fun clearRoles(userId: String)

    /**
     * 检查用户是否拥有指定角色
     */
    fun hasRole(userId: String, roleCode: String): Boolean

    /**
     * 检查用户是否拥有任意指定角色
     */
    fun hasAnyRole(userId: String, roleCodes: Set<String>): Boolean
}

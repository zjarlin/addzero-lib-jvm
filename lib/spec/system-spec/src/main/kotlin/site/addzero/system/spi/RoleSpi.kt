package site.addzero.system.spi

import site.addzero.system.model.common.PageResult
import site.addzero.system.spi.rbac.dto.RoleCreateRequest
import site.addzero.system.spi.rbac.dto.RoleDTO
import site.addzero.system.spi.rbac.dto.RoleQuery
import site.addzero.system.spi.rbac.dto.RoleUpdateRequest

/**
 * 角色服务SPI
 * 提供角色的增删改查及权限绑定功能
 */
interface RoleSpi {
    /**
     * 创建角色
     */
    fun create(request: RoleCreateRequest): RoleDTO

    /**
     * 更新角色
     */
    fun update(roleId: String, request: RoleUpdateRequest): RoleDTO

    /**
     * 删除角色
     */
    fun delete(roleId: String)

    /**
     * 根据ID查询角色
     */
    fun getById(roleId: String): RoleDTO?

    /**
     * 根据角色编码查询
     */
    fun getByCode(code: String): RoleDTO?

    /**
     * 分页查询角色
     */
    fun page(query: RoleQuery): PageResult<RoleDTO>

    /**
     * 查询全部角色
     */
    fun listAll(): List<RoleDTO>

    /**
     * 为角色分配权限
     */
    fun assignPermissions(roleId: String, permissionIds: Set<String>)

    /**
     * 获取角色拥有的权限ID列表
     */
    fun getPermissionIds(roleId: String): Set<String>
}

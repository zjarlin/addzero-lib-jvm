package site.addzero.system.spi.rbac.dto

import java.time.Instant

/**
 * 用户角色绑定数据传输对象
 */
data class UserRoleDTO(
    val userId: String,
    val roleId: String,
    val roleCode: String,
    val roleName: String,
    val assignedAt: Instant,
    val assignedBy: String?
)

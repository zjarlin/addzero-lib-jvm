package site.addzero.system.spi

/**
 * 权限校验服务SPI
 * 用于运行时权限检查
 */
interface PermissionCheckSpi {
    /**
     * 检查用户是否拥有指定权限
     */
    fun hasPermission(userId: String, permissionCode: String): Boolean

    /**
     * 检查用户是否拥有任意指定权限
     */
    fun hasAnyPermission(userId: String, permissionCodes: Set<String>): Boolean

    /**
     * 检查用户是否拥有所有指定权限
     */
    fun hasAllPermissions(userId: String, permissionCodes: Set<String>): Boolean

    /**
     * 获取用户的所有权限编码
     */
    fun getUserPermissions(userId: String): Set<String>

    /**
     * 校验权限，无权限时抛出异常
     */
    fun checkPermission(userId: String, permissionCode: String)

    /**
     * 校验任意权限，无权限时抛出异常
     */
    fun checkAnyPermission(userId: String, permissionCodes: Set<String>)
}

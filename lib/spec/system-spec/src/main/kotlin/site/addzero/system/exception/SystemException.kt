package site.addzero.system.exception

/**
 * 系统模块基础异常
 */
open class SystemException(
    message: String,
    val code: String = "SYSTEM_ERROR",
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ResourceNotFoundException(
    resource: String,
    identifier: String
) : SystemException(
    "$resource not found: $identifier",
    "RESOURCE_NOT_FOUND"
)

class PermissionDeniedException(
    message: String = "Permission denied"
) : SystemException(message, "PERMISSION_DENIED")

class DuplicateResourceException(
    resource: String,
    field: String
) : SystemException(
    "$resource already exists with $field",
    "DUPLICATE_RESOURCE"
)

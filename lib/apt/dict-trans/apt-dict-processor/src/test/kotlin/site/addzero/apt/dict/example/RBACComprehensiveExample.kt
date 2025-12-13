package site.addzero.apt.dict.example

import site.addzero.apt.dict.annotations.DictTranslate
import site.addzero.apt.dict.annotations.DictField

/**
 * Comprehensive RBAC (Role-Based Access Control) example demonstrating
 * compile-time dictionary translation with inheritance-based enhanced entities.
 * 
 * This example shows:
 * 1. T->R mapping where enhanced entities inherit from original entities
 * 2. System dictionary translation for status codes
 * 3. Table dictionary translation for foreign key relationships
 * 4. SPEL expression support for complex translations
 * 5. Batch translation optimization for N+1 query elimination
 */

// ============================================================================
// Original Entity Classes (T in T->R mapping)
// ============================================================================

/**
 * Base user entity with dictionary fields for translation
 */
@DictTranslate(suffix = "Enhanced")
open class User {
    var id: Long? = null
    var username: String? = null
    var email: String? = null
    
    @DictField(
        dictCode = "user_status",
        targetField = "statusText"
    )
    var status: String? = null
    
    @DictField(
        table = "sys_department",
        codeColumn = "id",
        nameColumn = "name",
        targetField = "departmentName"
    )
    var departmentId: Long? = null
    
    @DictField(
        table = "sys_organization",
        codeColumn = "id", 
        nameColumn = "full_name",
        condition = "status = 'ACTIVE'",
        targetField = "organizationName"
    )
    var organizationId: Long? = null
    
    var createdAt: java.time.LocalDateTime? = null
    var updatedAt: java.time.LocalDateTime? = null
}

/**
 * Role entity with system and table dictionary translations
 */
@DictTranslate(suffix = "Enhanced")
open class Role {
    var id: Long? = null
    var name: String? = null
    var code: String? = null
    
    @DictField(
        dictCode = "role_type",
        targetField = "typeText"
    )
    var type: String? = null
    
    @DictField(
        dictCode = "role_level",
        targetField = "levelText"
    )
    var level: String? = null
    
    @DictField(
        table = "sys_module",
        codeColumn = "id",
        nameColumn = "display_name",
        targetField = "moduleName"
    )
    var moduleId: Long? = null
    
    var description: String? = null
    var createdAt: java.time.LocalDateTime? = null
}

/**
 * Permission entity with complex SPEL expressions
 */
@DictTranslate(suffix = "Enhanced")
open class Permission {
    var id: Long? = null
    var name: String? = null
    var resource: String? = null
    var action: String? = null
    
    @DictField(
        dictCode = "permission_type",
        targetField = "typeText"
    )
    var type: String? = null
    
    @DictField(
        spelExp = "#{dict('resource_category', resource)}",
        targetField = "resourceCategoryText"
    )
    var resourceCategory: String? = null
    
    @DictField(
        spelExp = "#{table('sys_action', 'code', 'display_name', action)}",
        targetField = "actionText"
    )
    var actionCode: String? = null
    
    var createdAt: java.time.LocalDateTime? = null
}

/**
 * User-Role relationship entity with multiple dictionary translations
 */
@DictTranslate(suffix = "Enhanced")
open class UserRole {
    var id: Long? = null
    
    @DictField(
        table = "rbac_user",
        codeColumn = "id",
        nameColumn = "username",
        targetField = "userName"
    )
    var userId: Long? = null
    
    @DictField(
        table = "rbac_role", 
        codeColumn = "id",
        nameColumn = "name",
        targetField = "roleName"
    )
    var roleId: Long? = null
    
    @DictField(
        dictCode = "assignment_status",
        targetField = "statusText"
    )
    var status: String? = null
    
    @DictField(
        table = "rbac_user",
        codeColumn = "id", 
        nameColumn = "department_name",
        targetField = "assignerName"
    )
    var assignedBy: Long? = null
    
    var assignedAt: java.time.LocalDateTime? = null
    var expiresAt: java.time.LocalDateTime? = null
}

/**
 * Role-Permission relationship with conditional translations
 */
@DictTranslate(suffix = "Enhanced")
open class RolePermission {
    var id: Long? = null
    
    @DictField(
        table = "rbac_role",
        codeColumn = "id",
        nameColumn = "name", 
        targetField = "roleName"
    )
    var roleId: Long? = null
    
    @DictField(
        table = "rbac_permission",
        codeColumn = "id",
        nameColumn = "name",
        targetField = "permissionName"
    )
    var permissionId: Long? = null
    
    @DictField(
        dictCode = "grant_type",
        targetField = "grantTypeText"
    )
    var grantType: String? = null
    
    @DictField(
        spelExp = "#{dict('scope_type', grantType) + ' - ' + table('sys_scope', 'id', 'name', scopeId)}",
        targetField = "scopeDescription"
    )
    var scopeId: Long? = null
    
    var grantedAt: java.time.LocalDateTime? = null
    var grantedBy: Long? = null
}

/**
 * Audit log entity with comprehensive translation support
 */
@DictTranslate(suffix = "Enhanced")
open class AuditLog {
    var id: Long? = null
    
    @DictField(
        table = "rbac_user",
        codeColumn = "id",
        nameColumn = "username",
        targetField = "operatorName"
    )
    var operatorId: Long? = null
    
    @DictField(
        dictCode = "audit_action",
        targetField = "actionText"
    )
    var action: String? = null
    
    @DictField(
        dictCode = "audit_result",
        targetField = "resultText"
    )
    var result: String? = null
    
    @DictField(
        table = "sys_resource",
        codeColumn = "id",
        nameColumn = "display_name",
        targetField = "resourceName"
    )
    var resourceId: Long? = null
    
    @DictField(
        spelExp = "#{dict('severity_level', result)}",
        targetField = "severityText"
    )
    var severity: String? = null
    
    var details: String? = null
    var ipAddress: String? = null
    var userAgent: String? = null
    var createdAt: java.time.LocalDateTime? = null
}

// ============================================================================
// Enhanced Entity Usage Examples (R in T->R mapping)
// ============================================================================

/**
 * Example service demonstrating usage of enhanced entities
 */
class RBACService(private val transApi: site.addzero.apt.dict.context.TransApi) {
    
    /**
     * Example: Load user with all translations
     */
    fun loadUserWithTranslations(userId: Long): UserEnhanced {
        // Load original user data
        val user = loadUserFromDatabase(userId)
        
        // Create enhanced entity (inherits from User)
        val enhancedUser = UserEnhanced().apply {
            // Copy all original fields (inheritance provides access)
            id = user.id
            username = user.username
            email = user.email
            status = user.status
            departmentId = user.departmentId
            organizationId = user.organizationId
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }
        
        // Perform batch translation (eliminates N+1 queries)
        enhancedUser.translate(transApi)
        
        return enhancedUser
    }
    
    /**
     * Example: Load multiple users with batch translation optimization
     */
    fun loadUsersWithTranslations(userIds: List<Long>): List<UserEnhanced> {
        val users = loadUsersFromDatabase(userIds)
        
        return users.map { user ->
            val enhancedUser = UserEnhanced().apply {
                id = user.id
                username = user.username
                email = user.email
                status = user.status
                departmentId = user.departmentId
                organizationId = user.organizationId
                createdAt = user.createdAt
                updatedAt = user.updatedAt
            }
            
            // Each enhanced entity performs its own batch translation
            // The APT-generated code optimizes this to eliminate N+1 queries
            enhancedUser.translate(transApi)
            enhancedUser
        }
    }
    
    /**
     * Example: Complex RBAC query with multiple entity translations
     */
    fun getUserRolePermissions(userId: Long): RBACUserView {
        val user = loadUserWithTranslations(userId)
        val userRoles = loadUserRoles(userId).map { userRole ->
            UserRoleEnhanced().apply {
                id = userRole.id
                this.userId = userRole.userId
                roleId = userRole.roleId
                status = userRole.status
                assignedBy = userRole.assignedBy
                assignedAt = userRole.assignedAt
                expiresAt = userRole.expiresAt
                translate(transApi)
            }
        }
        
        val permissions = userRoles.flatMap { userRole ->
            loadRolePermissions(userRole.roleId!!).map { rolePermission ->
                RolePermissionEnhanced().apply {
                    id = rolePermission.id
                    this.roleId = rolePermission.roleId
                    permissionId = rolePermission.permissionId
                    grantType = rolePermission.grantType
                    scopeId = rolePermission.scopeId
                    grantedAt = rolePermission.grantedAt
                    grantedBy = rolePermission.grantedBy
                    translate(transApi)
                }
            }
        }
        
        return RBACUserView(user, userRoles, permissions)
    }
    
    /**
     * Example: Async translation for high-performance scenarios
     */
    suspend fun loadUserWithTranslationsAsync(userId: Long): UserEnhanced {
        val user = loadUserFromDatabase(userId)
        
        val enhancedUser = UserEnhanced().apply {
            id = user.id
            username = user.username
            email = user.email
            status = user.status
            departmentId = user.departmentId
            organizationId = user.organizationId
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }
        
        // Perform async translation
        enhancedUser.translateAsync(transApi).await()
        
        return enhancedUser
    }
    
    // Mock database methods
    private fun loadUserFromDatabase(userId: Long): User = User().apply { id = userId }
    private fun loadUsersFromDatabase(userIds: List<Long>): List<User> = userIds.map { User().apply { id = it } }
    private fun loadUserRoles(userId: Long): List<UserRole> = emptyList()
    private fun loadRolePermissions(roleId: Long): List<RolePermission> = emptyList()
}

/**
 * View object combining multiple enhanced entities
 */
data class RBACUserView(
    val user: UserEnhanced,
    val roles: List<UserRoleEnhanced>,
    val permissions: List<RolePermissionEnhanced>
)

// ============================================================================
// Dictionary Configuration Examples
// ============================================================================

/**
 * Example dictionary configurations that would be used by the system
 */
object RBACDictionaryConfig {
    
    /**
     * System dictionary codes used in RBAC entities
     */
    val SYSTEM_DICTIONARIES = mapOf(
        "user_status" to listOf("ACTIVE", "INACTIVE", "SUSPENDED", "PENDING"),
        "role_type" to listOf("SYSTEM", "BUSINESS", "FUNCTIONAL", "TEMPORARY"),
        "role_level" to listOf("L1", "L2", "L3", "L4", "L5"),
        "permission_type" to listOf("READ", "WRITE", "DELETE", "EXECUTE", "ADMIN"),
        "assignment_status" to listOf("ACTIVE", "EXPIRED", "REVOKED", "PENDING"),
        "grant_type" to listOf("DIRECT", "INHERITED", "CONDITIONAL", "TEMPORARY"),
        "audit_action" to listOf("LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "ACCESS"),
        "audit_result" to listOf("SUCCESS", "FAILURE", "WARNING", "ERROR"),
        "severity_level" to listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
    )
    
    /**
     * Table dictionary configurations
     */
    val TABLE_DICTIONARIES = mapOf(
        "sys_department" to TableConfig("id", "name", "status = 'ACTIVE'"),
        "sys_organization" to TableConfig("id", "full_name", "status = 'ACTIVE'"),
        "sys_module" to TableConfig("id", "display_name", "enabled = true"),
        "sys_action" to TableConfig("code", "display_name", ""),
        "sys_resource" to TableConfig("id", "display_name", "status = 'ACTIVE'"),
        "sys_scope" to TableConfig("id", "name", ""),
        "rbac_user" to TableConfig("id", "username", "status != 'DELETED'"),
        "rbac_role" to TableConfig("id", "name", "status = 'ACTIVE'"),
        "rbac_permission" to TableConfig("id", "name", "status = 'ACTIVE'")
    )
    
    data class TableConfig(
        val codeColumn: String,
        val nameColumn: String,
        val condition: String
    )
}

// ============================================================================
// Performance Optimization Examples
// ============================================================================

/**
 * Example showing how the APT processor optimizes translations
 */
class PerformanceOptimizationExample {
    
    /**
     * Before: N+1 query problem with reflection-based translation
     * 
     * For 100 users, this would result in:
     * - 1 query to load users
     * - 100 queries for user_status dictionary lookups
     * - 100 queries for department names
     * - 100 queries for organization names
     * Total: 301 queries
     */
    fun loadUsersWithReflection(userIds: List<Long>): List<User> {
        val users = loadUsersFromDatabase(userIds)
        // Each user would trigger separate dictionary queries
        return users // Without translations due to performance issues
    }
    
    /**
     * After: Batch optimization with APT-generated code
     * 
     * For 100 users, this results in:
     * - 1 query to load users
     * - 1 batch query for all user_status lookups
     * - 1 batch query for all department names
     * - 1 batch query for all organization names
     * Total: 4 queries (99.3% reduction!)
     */
    fun loadUsersWithAPTOptimization(userIds: List<Long>, transApi: site.addzero.apt.dict.context.TransApi): List<UserEnhanced> {
        val users = loadUsersFromDatabase(userIds)
        
        return users.map { user ->
            UserEnhanced().apply {
                // Copy fields (inheritance provides access)
                id = user.id
                username = user.username
                status = user.status
                departmentId = user.departmentId
                organizationId = user.organizationId
                
                // APT-generated translate() method uses batch queries
                translate(transApi)
            }
        }
    }
    
    private fun loadUsersFromDatabase(userIds: List<Long>): List<User> = emptyList()
}

// ============================================================================
// Testing Examples
// ============================================================================

/**
 * Example unit tests for enhanced entities
 */
class RBACEnhancedEntityTest {
    
    fun testUserEnhancedInheritance() {
        val enhanced = UserEnhanced()
        
        // Test inheritance - enhanced entity IS-A User
        assert(enhanced is User)
        
        // Test original fields are accessible
        enhanced.id = 1L
        enhanced.username = "testuser"
        enhanced.status = "ACTIVE"
        
        // Test translation fields are available
        enhanced.statusText = "Active User"
        enhanced.departmentName = "Engineering"
        
        assert(enhanced.id == 1L)
        assert(enhanced.username == "testuser")
        assert(enhanced.statusText == "Active User")
    }
    
    fun testBatchTranslationMetadata() {
        // Test that metadata constants are generated
        assert(UserEnhanced.SYSTEM_DICT_CODES.contains("user_status"))
        assert(UserEnhanced.TABLE_DICT_CONFIGS.contains("sys_department:id:name"))
        
        assert(RoleEnhanced.SYSTEM_DICT_CODES.contains("role_type"))
        assert(RoleEnhanced.SYSTEM_DICT_CODES.contains("role_level"))
    }
}
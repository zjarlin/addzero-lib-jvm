package site.addzero.system.spi.audit

import site.addzero.system.model.common.PageResult
import site.addzero.system.model.dto.AuditLogDTO
import site.addzero.system.model.dto.AuditLogQuery
import site.addzero.system.model.dto.AuditOperationRequest
import java.time.Instant

/**
 * 审计日志服务SPI
 * 记录系统操作日志，用于安全审计和追溯
 */
interface AuditLogSpi {

    /**
     * 记录操作日志
     * @param request 操作记录请求
     * @return 记录ID
     */
    fun record(request: AuditOperationRequest): String

    /**
     * 根据ID获取审计日志
     */
    fun getById(id: String): AuditLogDTO?

    /**
     * 分页查询审计日志
     */
    fun page(query: AuditLogQuery): PageResult<AuditLogDTO>

    /**
     * 查询用户的操作日志
     * @param userId 用户ID
     * @param limit 限制条数
     */
    fun getByUserId(userId: String, limit: Int = 100): List<AuditLogDTO>

    /**
     * 查询业务对象的变更记录
     * @param bizType 业务类型
     * @param bizId 业务ID
     */
    fun getByBizKey(bizType: String, bizId: String): List<AuditLogDTO>

    /**
     * 清理过期日志
     * @param before 清理此日期之前的日志
     * @return 清理的日志条数
     */
    fun clean(before: Instant): Int

    /**
     * 导出审计日志
     * @param query 查询条件
     * @param format 导出格式 (CSV, JSON, etc.)
     */
    fun export(query: AuditLogQuery, format: ExportFormat = ExportFormat.CSV): ByteArray
}

enum class ExportFormat {
    CSV,
    JSON,
    EXCEL
}

enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    EXPORT,
    IMPORT,
    EXECUTE,
    OTHER
}

enum class OperationStatus {
    SUCCESS,
    FAILURE,
    PENDING
}

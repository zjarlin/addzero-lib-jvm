package site.addzero.system.model.dto

import site.addzero.system.model.common.PageQuery
import site.addzero.system.spi.audit.OperationStatus
import site.addzero.system.spi.audit.OperationType
import java.time.Instant

/**
 * 审计日志DTO
 */
data class AuditLogDTO(
    val id: String,
    val traceId: String?,           // 链路追踪ID
    val userId: String?,            // 操作用户ID
    val username: String?,          // 操作用户名
    val userIp: String?,            // 用户IP
    val userAgent: String?,         // 用户代理
    val operationType: OperationType,
    val operationModule: String,    // 操作模块
    val operationAction: String,    // 操作动作
    val description: String?,       // 操作描述
    val requestParams: String?,     // 请求参数(JSON)
    val responseData: String?,      // 响应数据(JSON)
    val oldValue: String?,          // 变更前数据(JSON)
    val newValue: String?,          // 变更后数据(JSON)
    val bizType: String?,           // 业务类型
    val bizId: String?,             // 业务ID
    val status: OperationStatus,
    val errorMsg: String?,          // 错误信息
    val durationMs: Long,           // 执行耗时(ms)
    val serverIp: String?,          // 服务器IP
    val createdAt: Instant
)

data class AuditOperationRequest(
    val traceId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val userIp: String? = null,
    val userAgent: String? = null,
    val operationType: OperationType = OperationType.OTHER,
    val operationModule: String,
    val operationAction: String,
    val description: String? = null,
    val requestParams: Map<String, Any?>? = null,
    val responseData: Map<String, Any?>? = null,
    val oldValue: Map<String, Any?>? = null,
    val newValue: Map<String, Any?>? = null,
    val bizType: String? = null,
    val bizId: String? = null,
    val status: OperationStatus = OperationStatus.SUCCESS,
    val errorMsg: String? = null,
    val durationMs: Long = 0,
    val serverIp: String? = null
)

data class AuditLogQuery(
    override val pageNum: Int = 1,
    override val pageSize: Int = 10,
    val userId: String? = null,
    val username: String? = null,
    val operationType: OperationType? = null,
    val operationModule: String? = null,
    val operationAction: String? = null,
    val bizType: String? = null,
    val bizId: String? = null,
    val status: OperationStatus? = null,
    val userIp: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null
) : PageQuery(pageNum, pageSize)

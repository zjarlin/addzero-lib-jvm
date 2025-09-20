package site.addzero.cli.biz.task

import kotlinx.serialization.Serializable

/**
 * 任务状态
 */
@Serializable
enum class Status {
    PENDING,   // 待执行
    RUNNING,   // 执行中
    COMPLETED, // 已完成
    FAILED     // 失败
}

/**
 * 任务信息
 */
@Serializable
data class TaskInfo(
    val id: String,
    val name: String,
    val description: String = "",
    var status: Status = Status.PENDING,
    var errorMessage: String? = null,
    var startTime: Long? = null,
    var endTime: Long? = null
)


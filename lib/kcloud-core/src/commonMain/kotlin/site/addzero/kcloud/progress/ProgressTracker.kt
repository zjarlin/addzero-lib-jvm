package site.addzero.kcloud.progress

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlin.math.max
import kotlin.math.roundToInt

enum class TransferStage(val weight: Double) {
    PRECHECK(0.05),
    SCAN_LOCAL(0.10),
    TRANSFER(0.70),
    VERIFY(0.10),
    FINALIZE(0.05)
}

enum class TaskStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class StageUpdate(
    val taskId: String,
    val fileName: String,
    val stage: TransferStage,
    val stageProgress: Double,
    val transferredBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val errorMessage: String? = null
)

data class TaskProgress(
    val taskId: String,
    val fileName: String,
    val stage: TransferStage,
    val stagePercent: Int,
    val overallPercent: Int,
    val transferredBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val etaSeconds: Long,
    val status: TaskStatus = TaskStatus.RUNNING,
    val errorMessage: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

data class QueueProgress(
    val queueOverallPercent: Int,
    val activeTasks: List<TaskProgress>,
    val pendingTasks: List<TaskProgress>,
    val completedTasks: List<TaskProgress>,
    val failedTasks: List<TaskProgress>,
    val totalSpeedBytesPerSec: Long,
    val totalEtaSeconds: Long
)

class ProgressTracker {
    private val order = TransferStage.entries
    private val _tasks = MutableStateFlow<Map<String, TaskProgress>>(emptyMap())
    val tasks: StateFlow<Map<String, TaskProgress>> = _tasks.asStateFlow()

    private val _queueProgress = MutableStateFlow<QueueProgress?>(null)
    val queueProgress: StateFlow<QueueProgress?> = _queueProgress.asStateFlow()

    fun update(event: StageUpdate) {
        val normalized = event.stageProgress.coerceIn(0.0, 1.0)
        val completedWeight = order
            .takeWhile { it != event.stage }
            .sumOf { it.weight }

        val overall = ((completedWeight + event.stage.weight * normalized) * 100.0)
            .coerceIn(0.0, 100.0)
            .roundToInt()

        val stagePercent = (normalized * 100.0).roundToInt().coerceIn(0, 100)
        val existingTask = _tasks.value[event.taskId]

        val status = when {
            event.errorMessage != null -> TaskStatus.FAILED
            overall >= 100 -> TaskStatus.COMPLETED
            existingTask?.status == TaskStatus.PAUSED -> TaskStatus.PAUSED
            else -> TaskStatus.RUNNING
        }

        val snapshot = TaskProgress(
            taskId = event.taskId,
            fileName = event.fileName,
            stage = event.stage,
            stagePercent = stagePercent,
            overallPercent = overall,
            transferredBytes = max(0L, event.transferredBytes),
            totalBytes = max(0L, event.totalBytes),
            speedBytesPerSec = max(0L, event.speedBytesPerSec),
            etaSeconds = max(0L, event.etaSeconds),
            status = status,
            errorMessage = event.errorMessage,
            createdAt = existingTask?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )

        _tasks.value = _tasks.value.toMutableMap().apply {
            this[event.taskId] = snapshot
        }

        updateQueueProgress()
    }

    fun pauseTask(taskId: String) {
        _tasks.value[taskId]?.let { task ->
            _tasks.value = _tasks.value.toMutableMap().apply {
                this[taskId] = task.copy(status = TaskStatus.PAUSED, updatedAt = Clock.System.now().toEpochMilliseconds())
            }
            updateQueueProgress()
        }
    }

    fun resumeTask(taskId: String) {
        _tasks.value[taskId]?.let { task ->
            _tasks.value = _tasks.value.toMutableMap().apply {
                this[taskId] = task.copy(status = TaskStatus.RUNNING, updatedAt = Clock.System.now().toEpochMilliseconds())
            }
            updateQueueProgress()
        }
    }

    fun cancelTask(taskId: String) {
        _tasks.value[taskId]?.let { task ->
            _tasks.value = _tasks.value.toMutableMap().apply {
                this[taskId] = task.copy(status = TaskStatus.CANCELLED, updatedAt = Clock.System.now().toEpochMilliseconds())
            }
            updateQueueProgress()
        }
    }

    fun removeTask(taskId: String) {
        _tasks.value = _tasks.value.toMutableMap().apply {
            remove(taskId)
        }
        updateQueueProgress()
    }

    fun retryTask(taskId: String) {
        _tasks.value[taskId]?.let { task ->
            _tasks.value = _tasks.value.toMutableMap().apply {
                this[taskId] = task.copy(
                    status = TaskStatus.PENDING,
                    overallPercent = 0,
                    stagePercent = 0,
                    stage = TransferStage.PRECHECK,
                    errorMessage = null,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            }
            updateQueueProgress()
        }
    }

    private fun updateQueueProgress() {
        val allTasks = _tasks.value.values.toList()

        val activeTasks = allTasks.filter { it.status == TaskStatus.RUNNING }
        val pendingTasks = allTasks.filter { it.status == TaskStatus.PENDING }
        val completedTasks = allTasks.filter { it.status == TaskStatus.COMPLETED }
        val failedTasks = allTasks.filter { it.status == TaskStatus.FAILED }

        val totalBytes = allTasks.sumOf { it.totalBytes }
        val transferredBytes = allTasks.sumOf { it.transferredBytes }

        val queueOverallPercent = if (totalBytes > 0) {
            ((transferredBytes.toDouble() / totalBytes.toDouble()) * 100).roundToInt()
        } else {
            val totalTasks = allTasks.size
            val completedCount = completedTasks.size
            if (totalTasks > 0) ((completedCount.toDouble() / totalTasks) * 100).roundToInt() else 0
        }

        val totalSpeed = activeTasks.sumOf { it.speedBytesPerSec }

        val remainingBytes = totalBytes - transferredBytes
        val totalEta = if (totalSpeed > 0) remainingBytes / totalSpeed else 0L

        _queueProgress.value = QueueProgress(
            queueOverallPercent = queueOverallPercent.coerceIn(0, 100),
            activeTasks = activeTasks,
            pendingTasks = pendingTasks,
            completedTasks = completedTasks,
            failedTasks = failedTasks,
            totalSpeedBytesPerSec = totalSpeed,
            totalEtaSeconds = totalEta
        )
    }

    fun clearCompleted() {
        _tasks.value = _tasks.value.filterValues {
            it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED
        }.toMutableMap()
        updateQueueProgress()
    }

    fun getTask(taskId: String): TaskProgress? = _tasks.value[taskId]

    fun getAllTasks(): List<TaskProgress> = _tasks.value.values.toList()
}

object ProgressFormatter {
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024 * 1024 * 1024 -> "${(bytes / (1024.0 * 1024 * 1024 * 1024)).toInt()} TB"
            bytes >= 1024L * 1024 * 1024 -> "${(bytes / (1024.0 * 1024 * 1024)).toInt()} GB"
            bytes >= 1024L * 1024 -> "${(bytes / (1024.0 * 1024)).toInt()} MB"
            bytes >= 1024L -> "${(bytes / 1024.0).toInt()} KB"
            else -> "$bytes B"
        }
    }

    fun formatSpeed(bytesPerSec: Long): String {
        return formatBytes(bytesPerSec) + "/s"
    }

    fun formatDuration(seconds: Long): String {
        return when {
            seconds >= 3600 -> {
                val hours = seconds / 3600
                val mins = (seconds % 3600) / 60
                "${hours}h ${mins}m"
            }
            seconds >= 60 -> {
                val mins = seconds / 60
                val secs = seconds % 60
                "${mins}m ${secs}s"
            }
            else -> "${seconds}s"
        }
    }

    fun formatStageName(stage: TransferStage): String {
        return when (stage) {
            TransferStage.PRECHECK -> "预检查"
            TransferStage.SCAN_LOCAL -> "扫描本地文件"
            TransferStage.TRANSFER -> "传输中"
            TransferStage.VERIFY -> "验证中"
            TransferStage.FINALIZE -> "完成"
        }
    }

    fun formatStatusName(status: TaskStatus): String {
        return when (status) {
            TaskStatus.PENDING -> "等待中"
            TaskStatus.RUNNING -> "进行中"
            TaskStatus.PAUSED -> "已暂停"
            TaskStatus.COMPLETED -> "已完成"
            TaskStatus.FAILED -> "失败"
            TaskStatus.CANCELLED -> "已取消"
        }
    }
}

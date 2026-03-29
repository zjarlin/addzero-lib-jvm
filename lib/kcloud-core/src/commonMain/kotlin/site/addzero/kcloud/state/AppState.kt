package site.addzero.kcloud.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 同步状态枚举 - 对应托盘图标和UI显示
 */
enum class SyncStatus {
    IDLE,           // 空闲 - 绿色勾
    SCANNING,       // 扫描中 - 蓝色旋转
    UPLOADING,      // 上传中 - 蓝色进度
    DOWNLOADING,    // 下载中 - 蓝色进度
    CONFLICT,       // 有冲突 - 红色感叹号
    ERROR,          // 错误 - 红色叉
    PAUSED,         // 暂停 - 黄色暂停
    OFFLINE         // 离线 - 灰色
}

/**
 * 当前活跃窗口
 */
enum class ActiveWindow {
    NONE,       // 仅托盘
    MAIN,       // 主窗口
    SETTINGS,   // 设置窗口
    HISTORY,    // 历史记录窗口
    CONFLICT    // 冲突解决窗口
}

/**
 * 应用全局状态 - 所有UI组件订阅此状态
 */
data class AppState(
    // 同步状态
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val overallProgress: Float = 0f,        // 0.0 - 1.0
    val currentOperation: String? = null,    // "正在上传 document.pdf"

    // 统计
    val pendingUploads: Int = 0,
    val pendingDownloads: Int = 0,
    val conflictCount: Int = 0,

    // 连接状态
    val isOnline: Boolean = true,
    val lastSyncTime: Long? = null,

    // 窗口状态
    val isMainWindowVisible: Boolean = false,
    val activeWindow: ActiveWindow = ActiveWindow.MAIN
) {
    val hasConflicts: Boolean get() = conflictCount > 0
    val isBusy: Boolean get() = syncStatus in setOf(
        SyncStatus.SCANNING, SyncStatus.UPLOADING, SyncStatus.DOWNLOADING
    )
}

/**
 * 应用状态管理器 - 单例
 */
object AppStateManager {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // 状态更新方法
    fun updateSyncStatus(status: SyncStatus, message: String? = null) {
        _state.update { it.copy(
            syncStatus = status,
            currentOperation = message ?: it.currentOperation
        )}
    }

    fun updateProgress(progress: Float, operation: String? = null) {
        _state.update { it.copy(
            overallProgress = progress.coerceIn(0f, 1f),
            currentOperation = operation ?: it.currentOperation
        )}
    }

    fun setCurrentOperation(operation: String?) {
        _state.update { it.copy(currentOperation = operation) }
    }

    fun incrementPendingUploads() {
        _state.update { it.copy(pendingUploads = it.pendingUploads + 1) }
    }

    fun decrementPendingUploads() {
        _state.update { it.copy(pendingUploads = (it.pendingUploads - 1).coerceAtLeast(0)) }
    }

    fun incrementPendingDownloads() {
        _state.update { it.copy(pendingDownloads = it.pendingDownloads + 1) }
    }

    fun decrementPendingDownloads() {
        _state.update { it.copy(pendingDownloads = (it.pendingDownloads - 1).coerceAtLeast(0)) }
    }

    fun setConflictCount(count: Int) {
        _state.update { it.copy(conflictCount = count) }
    }

    fun addConflict() {
        _state.update { it.copy(conflictCount = it.conflictCount + 1) }
    }

    fun resolveConflict() {
        _state.update { it.copy(conflictCount = (it.conflictCount - 1).coerceAtLeast(0)) }
    }

    fun setOnline(online: Boolean) {
        _state.update {
            it.copy(
                isOnline = online,
                syncStatus = if (!online) SyncStatus.OFFLINE else if (it.syncStatus == SyncStatus.OFFLINE) SyncStatus.IDLE else it.syncStatus
            )
        }
    }

    fun setLastSyncTime(time: Long) {
        _state.update { it.copy(lastSyncTime = time) }
    }

    fun setMainWindowVisible(visible: Boolean) {
        _state.update { it.copy(isMainWindowVisible = visible) }
    }

    fun setActiveWindow(window: ActiveWindow) {
        _state.update { it.copy(activeWindow = window) }
    }

    // 便捷属性
    val currentState: AppState get() = _state.value
    val hasConflicts: Boolean get() = _state.value.hasConflicts
    val isBusy: Boolean get() = _state.value.isBusy
}

/**
 * 托盘图标状态 - 与SyncStatus映射
 */
enum class TrayIconState(
    val iconResource: String,
    val tooltipSuffix: String
) {
    IDLE("tray/icon_idle.png", "已同步"),
    SYNCING("tray/icon_syncing.gif", "同步中..."),
    WARNING("tray/icon_warning.png", "需要关注"),
    ERROR("tray/icon_error.png", "同步错误"),
    PAUSED("tray/icon_paused.png", "已暂停");

    companion object {
        fun fromSyncStatus(status: SyncStatus): TrayIconState = when (status) {
            SyncStatus.IDLE -> IDLE
            SyncStatus.SCANNING, SyncStatus.UPLOADING, SyncStatus.DOWNLOADING -> SYNCING
            SyncStatus.CONFLICT -> WARNING
            SyncStatus.ERROR -> ERROR
            SyncStatus.PAUSED, SyncStatus.OFFLINE -> PAUSED
        }
    }
}

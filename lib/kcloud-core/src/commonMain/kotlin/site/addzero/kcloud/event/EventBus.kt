package site.addzero.kcloud.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * UI事件定义 - 所有跨组件通信通过此事件总线
 */
sealed class UIEvent {
    // ========== 同步事件 ==========
    data class SyncStarted(val fileCount: Int, val operation: String) : UIEvent()
    data class SyncProgress(
        val currentFile: Int,
        val totalFiles: Int,
        val fileName: String,
        val progress: Float
    ) : UIEvent()
    data class SyncCompleted(
        val uploaded: Int,
        val downloaded: Int,
        val durationMs: Long
    ) : UIEvent()
    data class SyncError(val message: String, val recoverable: Boolean = true) : UIEvent()
    data class SyncPaused(val reason: String? = null) : UIEvent()
    data class SyncResumed(val message: String? = null) : UIEvent()

    // ========== 冲突事件 ==========
    data class ConflictDetected(val conflicts: List<site.addzero.kcloud.model.Conflict>) : UIEvent()
    data class ConflictResolved(val conflictId: String, val resolution: site.addzero.kcloud.db.ConflictResolution) : UIEvent()

    // ========== 窗口事件 ==========
    object WindowShouldShow : UIEvent()
    object WindowShouldHide : UIEvent()
    object WindowShouldToggle : UIEvent()
    data class NavigateTo(val screen: String) : UIEvent()

    // ========== 托盘事件 ==========
    data class TrayNotification(
        val title: String,
        val message: String,
        val type: NotificationType = NotificationType.INFO
    ) : UIEvent()
    data class TrayBadgeUpdate(val count: Int) : UIEvent()
    data class TrayTooltipUpdate(val text: String) : UIEvent()

    // ========== 设置事件 ==========
    data class SettingsChanged(val key: String, val value: Any) : UIEvent()
    object SettingsShouldOpen : UIEvent()

    // ========== 文件事件 ==========
    data class FileUploaded(val path: String, val remotePath: String) : UIEvent()
    data class FileDownloaded(val remotePath: String, val localPath: String) : UIEvent()
    data class FileConflict(val localPath: String, val remotePath: String) : UIEvent()

    // ========== 连接事件 ==========
    object ConnectionLost : UIEvent()
    object ConnectionRestored : UIEvent()
    data class ConnectionError(val error: String) : UIEvent()

    // ========== 存储后端事件 ==========
    data class StorageBackendFailed(val backendType: String) : UIEvent()
    data class StorageBackendRecovered(val backendType: String) : UIEvent()
    data class FailoverActivated(val fromBackend: String, val toBackend: String) : UIEvent()
    data class FailoverRecovered(val primaryBackend: String) : UIEvent()

    // ========== 更新事件 ==========
    data class UpdateAvailable(
        val version: String,
        val releaseNotes: String,
        val mandatory: Boolean
    ) : UIEvent()
    data class UpdateDownloadProgress(val percentage: Int) : UIEvent()
    data class UpdateDownloaded(val filePath: String) : UIEvent()
    data class UpdateInstalled(val version: String) : UIEvent()
    data class UpdateError(val message: String) : UIEvent()
}

/**
 * 通知类型
 */
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * 全局事件总线 - 单例
 */
object EventBus {
    private val _events = MutableSharedFlow<UIEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<UIEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * 发送事件
     */
    fun emit(event: UIEvent) {
        _events.tryEmit(event)
    }

    /**
     * 订阅事件
     */
    fun subscribe(
        scope: CoroutineScope,
        onEvent: (UIEvent) -> Unit
    ) = events.collectIn(scope, onEvent)

    /**
     * 订阅特定类型事件
     */
    inline fun <reified T : UIEvent> subscribeOf(
        scope: CoroutineScope,
        crossinline onEvent: (T) -> Unit
    ) = scope.launch {
        events.collect { event ->
            if (event is T) onEvent(event)
        }
    }
}

/**
 * 便捷扩展：在指定Scope中收集Flow
 */
fun <T> SharedFlow<T>.collectIn(
    scope: CoroutineScope,
    action: (T) -> Unit
) = scope.launch {
    collect(action)
}

/**
 * 便捷扩展：发送事件的简写
 */
fun sendEvent(event: UIEvent) = EventBus.emit(event)

/**
 * 预定义的快捷事件发送方法
 */
object EventShortcuts {
    fun syncStarted(fileCount: Int, operation: String = "开始同步") {
        sendEvent(UIEvent.SyncStarted(fileCount, operation))
    }

    fun syncProgress(current: Int, total: Int, fileName: String, progress: Float) {
        sendEvent(UIEvent.SyncProgress(current, total, fileName, progress))
    }

    fun syncCompleted(uploaded: Int, downloaded: Int, durationMs: Long) {
        sendEvent(UIEvent.SyncCompleted(uploaded, downloaded, durationMs))
    }

    fun syncError(message: String, recoverable: Boolean = true) {
        sendEvent(UIEvent.SyncError(message, recoverable))
    }

    fun notify(title: String, message: String, type: NotificationType = NotificationType.INFO) {
        sendEvent(UIEvent.TrayNotification(title, message, type))
    }

    fun notifySuccess(title: String, message: String) {
        sendEvent(UIEvent.TrayNotification(title, message, NotificationType.SUCCESS))
    }

    fun notifyError(title: String, message: String) {
        sendEvent(UIEvent.TrayNotification(title, message, NotificationType.ERROR))
    }

    fun showWindow() {
        sendEvent(UIEvent.WindowShouldShow)
    }

    fun hideWindow() {
        sendEvent(UIEvent.WindowShouldHide)
    }

    fun toggleWindow() {
        sendEvent(UIEvent.WindowShouldToggle)
    }
}

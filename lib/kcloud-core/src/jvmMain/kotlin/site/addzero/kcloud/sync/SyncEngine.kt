package site.addzero.kcloud.sync

import site.addzero.kcloud.db.*
import site.addzero.kcloud.event.*
import site.addzero.kcloud.model.Conflict
import site.addzero.kcloud.model.ConflictStrategy
import site.addzero.kcloud.state.AppStateManager
import site.addzero.kcloud.state.SyncStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import site.addzero.system.spi.*
import kotlin.math.roundToInt

/**
 * 同步计划 - 包含所有需要执行的操作
 */
data class SyncPlan(
    val toUpload: List<FileChange.NewLocal> = emptyList(),
    val toDownload: List<FileChange.NewRemote> = emptyList(),
    val toDeleteLocal: List<FileChange.DeletedRemote> = emptyList(),
    val toDeleteRemote: List<FileChange.DeletedLocal> = emptyList(),
    val conflicts: List<FileChange.Conflict> = emptyList(),
    val modifiedLocal: List<FileChange.ModifiedLocal> = emptyList(),
    val modifiedRemote: List<FileChange.ModifiedRemote> = emptyList()
) {
    val isEmpty: Boolean get() = toUpload.isEmpty() && toDownload.isEmpty() &&
            toDeleteLocal.isEmpty() && toDeleteRemote.isEmpty() &&
            conflicts.isEmpty() && modifiedLocal.isEmpty() && modifiedRemote.isEmpty()

    val totalOperations: Int get() = toUpload.size + toDownload.size +
            toDeleteLocal.size + toDeleteRemote.size + conflicts.size +
            modifiedLocal.size + modifiedRemote.size
}

/**
 * 同步结果
 */
data class SyncResult(
    val success: Boolean,
    val uploaded: Int = 0,
    val downloaded: Int = 0,
    val deleted: Int = 0,
    val conflicts: Int = 0,
    val errors: List<String> = emptyList(),
    val durationMs: Long = 0
)

/**
 * 同步引擎 - 核心同步逻辑
 */
class SyncEngine(
    private val database: Database,
    private val storageClient: StorageClient,
    private val syncRoot: String,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var isRunning = false
    private var isPaused = false
    private var currentJob: Job? = null

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /**
     * 启动同步引擎
     */
    fun start() {
        scope.launch {
            while (isActive) {
                if (!isPaused && !isRunning) {
                    val pendingItems = database.getPendingQueueItems(1)
                    if (pendingItems.isNotEmpty()) {
                        processSyncQueue()
                    }
                }
                delay(5000) // 每5秒检查一次队列
            }
        }
    }

    /**
     * 停止同步引擎
     */
    fun stop() {
        currentJob?.cancel()
        scope.cancel()
    }

    /**
     * 暂停同步
     */
    fun pause() {
        isPaused = true
        AppStateManager.updateSyncStatus(SyncStatus.PAUSED, "同步已暂停")
        EventBus.emit(UIEvent.SyncPaused())
    }

    /**
     * 恢复同步
     */
    fun resume() {
        isPaused = false
        AppStateManager.updateSyncStatus(SyncStatus.IDLE, "同步已恢复")
        EventBus.emit(UIEvent.SyncResumed())
    }

    /**
     * 触发立即同步
     */
    suspend fun syncNow(): SyncResult = withContext(scope.coroutineContext) {
        val startTime = System.currentTimeMillis()

        try {
            AppStateManager.updateSyncStatus(SyncStatus.SCANNING, "正在扫描本地文件...")

            // 1. 检测本地变化
            val localChanges = detectLocalChanges()

            // 2. 检测远程变化
            AppStateManager.updateSyncStatus(SyncStatus.SCANNING, "正在获取远程文件列表...")
            val remoteChanges = detectRemoteChanges()

            // 3. 生成同步计划
            val plan = buildSyncPlan(localChanges, remoteChanges)

            // 4. 处理冲突
            if (plan.conflicts.isNotEmpty()) {
                handleConflicts(plan.conflicts)
            }

            // 5. 执行同步
            return@withContext executeSyncPlan(plan)

        } catch (e: Exception) {
            val errorMsg = "同步失败: ${e.message}"
            AppStateManager.updateSyncStatus(SyncStatus.ERROR, errorMsg)
            EventShortcuts.notifyError("同步错误", errorMsg)

            return@withContext SyncResult(
                success = false,
                errors = listOf(errorMsg),
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * 检测本地变化
     */
    suspend fun detectLocalChanges(): List<FileChange> = withContext(Dispatchers.IO) {
        val changes = mutableListOf<FileChange>()
        val syncDir = java.io.File(syncRoot)

        if (!syncDir.exists()) {
            syncDir.mkdirs()
            return@withContext changes
        }

        // 扫描本地所有文件
        val localFiles = scanLocalFiles(syncDir)
        val dbRecords = database.getAllFileRecords().associateBy { it.path }

        // 检测新增和修改
        localFiles.forEach { (relativePath, fileInfo) ->
            val record = dbRecords[relativePath]

            when {
                record == null -> {
                    // 新增文件
                    changes.add(FileChange.NewLocal(
                        path = relativePath,
                        size = fileInfo.size,
                        mtime = fileInfo.mtime
                    ))
                }
                record.localMtime != fileInfo.mtime || record.localSize != fileInfo.size -> {
                    // 可能修改，需要计算哈希确认
                    val currentHash = computeFileHash(fileInfo.file)
                    if (record.localHash != currentHash) {
                        changes.add(FileChange.ModifiedLocal(
                            path = relativePath,
                            size = fileInfo.size,
                            mtime = fileInfo.mtime,
                            oldHash = record.localHash
                        ))
                    }
                }
            }
        }

        // 检测删除
        dbRecords.forEach { (path, record) ->
            if (!localFiles.containsKey(path)) {
                changes.add(FileChange.DeletedLocal(path))
            }
        }

        changes
    }

    /**
     * 检测远程变化
     */
    suspend fun detectRemoteChanges(): List<FileChange> = withContext(Dispatchers.IO) {
        val changes = mutableListOf<FileChange>()

        try {
            val remoteObjects = storageClient.listObjects()
            val dbRecords = database.getAllFileRecords().associateBy { it.path }

            // 检测远程新增和修改
            remoteObjects.forEach { remote ->
                val record = dbRecords[remote.key]

                when {
                    record == null -> {
                        changes.add(FileChange.NewRemote(
                            path = remote.key,
                            size = remote.size,
                            etag = remote.etag,
                            mtime = remote.lastModified
                        ))
                    }
                    record.remoteEtag != remote.etag -> {
                        changes.add(FileChange.ModifiedRemote(
                            path = remote.key,
                            size = remote.size,
                            etag = remote.etag,
                            oldEtag = record.remoteEtag,
                            mtime = remote.lastModified
                        ))
                    }
                }
            }

            // 检测远程删除
            val remotePaths = remoteObjects.map { it.key }.toSet()
            dbRecords.forEach { (path, record) ->
                if (path !in remotePaths && record.remoteEtag != null) {
                    changes.add(FileChange.DeletedRemote(path))
                }
            }

        } catch (e: Exception) {
            AppStateManager.setOnline(false)
            throw e
        }

        changes
    }

    /**
     * 构建同步计划
     */
    fun buildSyncPlan(
        localChanges: List<FileChange>,
        remoteChanges: List<FileChange>
    ): SyncPlan {
        val toUpload = mutableListOf<FileChange.NewLocal>()
        val toDownload = mutableListOf<FileChange.NewRemote>()
        val toDeleteLocal = mutableListOf<FileChange.DeletedRemote>()
        val toDeleteRemote = mutableListOf<FileChange.DeletedLocal>()
        val conflicts = mutableListOf<FileChange.Conflict>()
        val modifiedLocal = mutableListOf<FileChange.ModifiedLocal>()
        val modifiedRemote = mutableListOf<FileChange.ModifiedRemote>()

        val localMap = localChanges.associateBy { it.path }
        val remoteMap = remoteChanges.associateBy { it.path }

        // 处理所有路径
        val allPaths = (localMap.keys + remoteMap.keys).toSortedSet()

        allPaths.forEach { path ->
            val local = localMap[path]
            val remote = remoteMap[path]

            when {
                // 本地和远程都有变化 = 冲突
                local != null && remote != null -> {
                    when {
                        local is FileChange.NewLocal && remote is FileChange.NewRemote -> {
                            conflicts.add(FileChange.Conflict(
                                path = path,
                                localMtime = local.mtime,
                                remoteMtime = remote.mtime,
                                localSize = local.size,
                                remoteSize = remote.size
                            ))
                        }
                        local is FileChange.ModifiedLocal && remote is FileChange.ModifiedRemote -> {
                            conflicts.add(FileChange.Conflict(
                                path = path,
                                localMtime = local.mtime,
                                remoteMtime = remote.mtime,
                                localSize = local.size,
                                remoteSize = remote.size
                            ))
                        }
                        // 一方删除，一方修改
                        local is FileChange.DeletedLocal && remote is FileChange.ModifiedRemote -> {
                            conflicts.add(FileChange.Conflict(
                                path = path,
                                localMtime = 0,
                                remoteMtime = remote.mtime,
                                localSize = 0,
                                remoteSize = remote.size
                            ))
                        }
                        local is FileChange.ModifiedLocal && remote is FileChange.DeletedRemote -> {
                            conflicts.add(FileChange.Conflict(
                                path = path,
                                localMtime = local.mtime,
                                remoteMtime = 0,
                                localSize = local.size,
                                remoteSize = 0
                            ))
                        }
                        // 双方都删除 - 无需操作
                        local is FileChange.DeletedLocal && remote is FileChange.DeletedRemote -> {
                            // 从数据库中删除记录
                            database.deleteFileRecord(path)
                        }
                    }
                }
                // 只有本地变化
                local != null -> {
                    when (local) {
                        is FileChange.NewLocal -> toUpload.add(local)
                        is FileChange.ModifiedLocal -> modifiedLocal.add(local)
                        is FileChange.DeletedLocal -> toDeleteRemote.add(local)
                        else -> {}
                    }
                }
                // 只有远程变化
                remote != null -> {
                    when (remote) {
                        is FileChange.NewRemote -> toDownload.add(remote)
                        is FileChange.ModifiedRemote -> modifiedRemote.add(remote)
                        is FileChange.DeletedRemote -> toDeleteLocal.add(remote)
                        else -> {}
                    }
                }
            }
        }

        return SyncPlan(
            toUpload = toUpload,
            toDownload = toDownload,
            toDeleteLocal = toDeleteLocal,
            toDeleteRemote = toDeleteRemote,
            conflicts = conflicts,
            modifiedLocal = modifiedLocal,
            modifiedRemote = modifiedRemote
        )
    }

    /**
     * 执行同步计划
     */
    private suspend fun executeSyncPlan(plan: SyncPlan): SyncResult = withContext(scope.coroutineContext) {
        val startTime = System.currentTimeMillis()
        var uploaded = 0
        var downloaded = 0
        var deleted = 0
        val errors = mutableListOf<String>()

        val totalOperations = plan.totalOperations
        if (totalOperations == 0) {
            return@withContext SyncResult(success = true, durationMs = 0)
        }

        EventShortcuts.syncStarted(totalOperations, "开始同步")
        AppStateManager.updateSyncStatus(SyncStatus.UPLOADING, "正在同步...")

        var completed = 0

        // 处理上传（包括修改的文件）
        val uploadList = plan.toUpload + plan.modifiedLocal.map {
            FileChange.NewLocal(it.path, it.size, it.mtime)
        }

        uploadList.forEach { change ->
            if (isPaused) {
                delay(100)
                return@forEach
            }

            try {
                val localFile = java.io.File(syncRoot, change.path)
                val result = storageClient.uploadObject(
                    localPath = localFile.absolutePath,
                    remotePath = change.path,
                    progress = { transferred, total ->
                        val fileProgress = if (total > 0) transferred.toFloat() / total else 0f
                        val overallProgress = (completed + fileProgress) / totalOperations
                        AppStateManager.updateProgress(overallProgress, "正在上传 ${change.path}")
                    }
                )

                if (result.success) {
                    // 更新数据库
                    database.updateLocalInfo(
                        path = change.path,
                        mtime = change.mtime,
                        size = change.size,
                        hash = computeFileHash(localFile)
                    )
                    database.updateRemoteInfo(
                        path = change.path,
                        etag = result.etag ?: "",
                        versionId = result.versionId,
                        mtime = System.currentTimeMillis(),
                        size = change.size
                    )
                    database.updateSyncState(change.path, SyncState.SYNCED, System.currentTimeMillis())
                    uploaded++
                } else {
                    errors.add("上传失败 ${change.path}: ${result.error}")
                }
            } catch (e: Exception) {
                errors.add("上传异常 ${change.path}: ${e.message}")
            }

            completed++
        }

        // 处理下载
        AppStateManager.updateSyncStatus(SyncStatus.DOWNLOADING, "正在下载...")

        plan.toDownload.forEach { change ->
            if (isPaused) {
                delay(100)
                return@forEach
            }

            try {
                val localFile = java.io.File(syncRoot, change.path)
                localFile.parentFile?.mkdirs()

                val result = storageClient.downloadObject(
                    remotePath = change.path,
                    localPath = localFile.absolutePath,
                    progress = { transferred, total ->
                        val fileProgress = if (total > 0) transferred.toFloat() / total else 0f
                        val overallProgress = (completed + fileProgress) / totalOperations
                        AppStateManager.updateProgress(overallProgress, "正在下载 ${change.path}")
                    }
                )

                if (result.success) {
                    val mtime = localFile.lastModified()
                    database.updateLocalInfo(
                        path = change.path,
                        mtime = mtime,
                        size = localFile.length(),
                        hash = computeFileHash(localFile)
                    )
                    database.updateRemoteInfo(
                        path = change.path,
                        etag = change.etag,
                        versionId = null,
                        mtime = change.mtime,
                        size = change.size
                    )
                    database.updateSyncState(change.path, SyncState.SYNCED, System.currentTimeMillis())
                    downloaded++
                } else {
                    errors.add("下载失败 ${change.path}: ${result.error}")
                }
            } catch (e: Exception) {
                errors.add("下载异常 ${change.path}: ${e.message}")
            }

            completed++
        }

        // 处理本地删除（远程文件已删除）
        plan.toDeleteLocal.forEach { change ->
            try {
                val localFile = java.io.File(syncRoot, change.path)
                if (localFile.delete()) {
                    database.deleteFileRecord(change.path)
                    deleted++
                }
            } catch (e: Exception) {
                errors.add("删除本地文件失败 ${change.path}: ${e.message}")
            }
            completed++
        }

        // 处理远程删除（本地文件已删除）
        plan.toDeleteRemote.forEach { change ->
            try {
                if (storageClient.deleteObject(change.path)) {
                    database.deleteFileRecord(change.path)
                    deleted++
                }
            } catch (e: Exception) {
                errors.add("删除远程文件失败 ${change.path}: ${e.message}")
            }
            completed++
        }

        val duration = System.currentTimeMillis() - startTime

        // 更新状态
        if (errors.isEmpty()) {
            AppStateManager.updateSyncStatus(SyncStatus.IDLE, "同步完成")
            AppStateManager.setLastSyncTime(System.currentTimeMillis())
            EventShortcuts.syncCompleted(uploaded, downloaded, duration)
        } else {
            AppStateManager.updateSyncStatus(SyncStatus.ERROR, "同步完成但有错误")
            EventShortcuts.notifyError("同步完成", "有 ${errors.size} 个错误")
        }

        SyncResult(
            success = errors.isEmpty(),
            uploaded = uploaded,
            downloaded = downloaded,
            deleted = deleted,
            conflicts = plan.conflicts.size,
            errors = errors,
            durationMs = duration
        )
    }

    /**
     * 处理冲突
     */
    private fun handleConflicts(conflicts: List<FileChange.Conflict>) {
        conflicts.forEach { conflict ->
            // 更新数据库状态为冲突
            database.updateSyncState(conflict.path, SyncState.CONFLICT)

            // 如果没有预设策略，使用默认策略
            val record = database.getFileRecord(conflict.path)
            if (record?.conflictStrategy == null) {
                // 默认使用较新的文件
                val strategy = if (conflict.localMtime > conflict.remoteMtime) {
                    ConflictStrategy.OVERWRITE
                } else {
                    ConflictStrategy.SKIP
                }
                database.setConflictStrategy(conflict.path, strategy)
            }
        }

        AppStateManager.setConflictCount(conflicts.size)

        // 发送冲突事件
        EventBus.emit(UIEvent.ConflictDetected(
            conflicts.map {
                Conflict(
                    id = generateConflictId(),
                    path = it.path,
                    localVersion = "",
                    remoteVersion = "",
                    timestamp = System.currentTimeMillis()
                )
            }
        ))
    }

    /**
     * 处理同步队列
     */
    private suspend fun processSyncQueue() {
        // 队列处理逻辑（用于后台自动同步）
        // TODO: 实现队列处理
    }

    /**
     * 解决文件冲突
     *
     * @param path 冲突文件路径
     * @param resolution 解决策略
     */
    suspend fun resolveConflict(
        path: String,
        resolution: ConflictResolution
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val record = database.getFileRecord(path)
                ?: return@withContext Result.failure(IllegalStateException("文件记录不存在: $path"))

            if (record.syncState != SyncState.CONFLICT) {
                return@withContext Result.failure(IllegalStateException("文件不是冲突状态: $path"))
            }

            when (resolution) {
                ConflictResolution.USE_LOCAL -> {
                    // 使用本地版本，强制上传
                    database.updateSyncState(path, SyncState.PENDING_UPLOAD)
                    // 触发上传
                    uploadFile(path)
                }
                ConflictResolution.USE_REMOTE -> {
                    // 使用远程版本，强制下载
                    database.updateSyncState(path, SyncState.PENDING_DOWNLOAD)
                    // 触发下载
                    downloadFile(path)
                }
                ConflictResolution.KEEP_BOTH -> {
                    // 保留两者，重命名本地文件
                    val localFile = java.io.File(syncRoot, path)
                    val renamedPath = "$path.conflict-${System.currentTimeMillis()}"
                    val renamedFile = java.io.File(syncRoot, renamedPath)

                    if (localFile.renameTo(renamedFile)) {
                        // 原路径标记为下载远程版本
                        database.updateSyncState(path, SyncState.PENDING_DOWNLOAD)
                        // 重命名的文件标记为上传
                        database.updateLocalInfo(
                            path = renamedPath,
                            mtime = renamedFile.lastModified(),
                            size = renamedFile.length(),
                            hash = computeFileHash(renamedFile)
                        )
                        database.updateSyncState(renamedPath, SyncState.PENDING_UPLOAD)

                        // 触发下载和上传
                        downloadFile(path)
                        uploadFile(renamedPath)
                    }
                }
                ConflictResolution.MERGE -> {
                    // 合并策略（仅文本文件支持）
                    // TODO: 实现三路合并
                    return@withContext Result.failure(NotImplementedError("合并功能尚未实现"))
                }
            }

            // 更新冲突计数
            val conflicts = database.getFileRecordsByState(SyncState.CONFLICT)
            AppStateManager.setConflictCount(conflicts.size)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传文件
     */
    private suspend fun uploadFile(path: String) {
        val fullPath = java.io.File(syncRoot, path).absolutePath
        val result = storageClient.uploadObject(
            localPath = fullPath,
            remotePath = path
        ) { transferred, total ->
            // 更新进度
            val progress = if (total > 0) transferred.toFloat() / total else 0f
            AppStateManager.updateProgress(progress, "上传: $path")
        }

        if (result.success) {
            database.updateRemoteInfo(
                path = path,
                etag = result.etag ?: "",
                versionId = result.versionId,
                mtime = System.currentTimeMillis(),
                size = java.io.File(fullPath).length()
            )
            database.updateSyncState(path, SyncState.SYNCED, System.currentTimeMillis())
        } else {
            throw IllegalStateException("上传失败: ${result.error}")
        }
    }

    /**
     * 下载文件
     */
    private suspend fun downloadFile(path: String) {
        val fullPath = java.io.File(syncRoot, path).absolutePath
        val result = storageClient.downloadObject(
            remotePath = path,
            localPath = fullPath
        ) { downloaded, total ->
            // 更新进度
            val progress = if (total > 0) downloaded.toFloat() / total else 0f
            AppStateManager.updateProgress(progress, "下载: $path")
        }

        if (result.success) {
            val localFile = java.io.File(fullPath)
            database.updateLocalInfo(
                path = path,
                mtime = localFile.lastModified(),
                size = localFile.length(),
                hash = computeFileHash(localFile)
            )
            database.updateSyncState(path, SyncState.SYNCED, System.currentTimeMillis())
        } else {
            throw IllegalStateException("下载失败: ${result.error}")
        }
    }

    /**
     * 扫描本地文件
     */
    private fun scanLocalFiles(dir: java.io.File): Map<String, FileInfo> {
        val files = mutableMapOf<String, FileInfo>()

        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relativePath = file.relativeTo(dir).path.replace("\\", "/")
                files[relativePath] = FileInfo(
                    file = file,
                    size = file.length(),
                    mtime = file.lastModified()
                )
            }
        }

        return files
    }

    /**
     * 计算文件哈希
     */
    private fun computeFileHash(file: java.io.File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun generateConflictId(): String {
        return "conflict_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    data class FileInfo(
        val file: java.io.File,
        val size: Long,
        val mtime: Long
    )
}

/**
 * 同步引擎管理器 - 单例
 */
object SyncEngineManager {
    private var instance: SyncEngine? = null

    fun initialize(
        database: Database,
        storageClient: StorageClient,
        syncRoot: String
    ): SyncEngine {
        if (instance == null) {
            instance = SyncEngine(database, storageClient, syncRoot).apply {
                start()
            }
        }
        return instance!!
    }

    fun get(): SyncEngine {
        return instance ?: throw IllegalStateException("SyncEngine未初始化")
    }

    fun stop() {
        instance?.stop()
        instance = null
    }
}

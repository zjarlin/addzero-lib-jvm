package site.addzero.cli.biz.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.setting.SettingContext
import site.addzero.core.ext.toJsonByKtx
import site.addzero.core.network.json.json

val taskStatusService: TaskStatusService by inject(TaskStatusService::class.java)

/**
 * 任务状态管理器
 */
@Single
class TaskStatusService {
    private val tasks = mutableMapOf<String, TaskInfo>()

    /**
     * 初始化状态目录
     */
    suspend fun initStatusDir() = withContext(Dispatchers.IO) {
        PlatformService.mkdir(SettingContext.STATUS_DIR)
    }

    /**
     * 加载任务状态
     */
    suspend fun loadTaskStatus() = withContext(Dispatchers.IO) {
        if (PlatformService.fileExists(SettingContext.STATUS_FILE)) {
            try {
                val content = PlatformService.readFile(SettingContext.STATUS_FILE)
                val loadedTasks = json.decodeFromString<Map<String, TaskInfo>>(content)
                tasks.clear()
                tasks.putAll(loadedTasks)
                true
            } catch (e: Exception) {
                println("加载任务状态失败: ${e.message}")
                false
            }
        } else {
            // 文件不存在，初始化空状态
            saveTaskStatus()
        }
    }

    /**
     * 保存任务状态
     */
    suspend fun saveTaskStatus() = withContext(Dispatchers.IO) {
        try {
            val content = tasks.toJsonByKtx()
            val result = PlatformService.writeFile(SettingContext.STATUS_FILE, content)
            if (!result) {
                println("保存任务状态失败")
            }
            result
        } catch (e: Exception) {
            println("保存任务状态失败: ${e.message}")
            false
        }
    }

    /**
     * 添加任务
     */
    suspend fun addTask(task: TaskInfo) = withContext(Dispatchers.IO) {
        tasks[task.id] = task
        saveTaskStatus()
    }

    /**
     * 更新任务状态
     */
    suspend fun updateTaskStatus(id: String, status: Status, errorMessage: String? = null) =
        withContext(Dispatchers.IO) {
            val task = tasks[id]
            if (task != null) {
                task.status = status
                if (status == Status.RUNNING && task.startTime == null) {
                    task.startTime = System.currentTimeMillis()
                } else if ((status == Status.COMPLETED || status == Status.FAILED) && task.endTime == null) {
                    task.endTime = System.currentTimeMillis()
                }

                if (errorMessage != null) {
                    task.errorMessage = errorMessage
                }

                saveTaskStatus()
                true
            } else {
                false
            }
        }

    /**
     * 获取任务状态
     */
    fun getTaskStatus(id: String): Status? {
        return tasks[id]?.status
    }

    /**
     * 获取任务信息
     */
    fun getTaskInfo(id: String): TaskInfo? {
        return tasks[id]
    }

    /**
     * 获取所有任务
     */
    fun getAllTasks(): List<TaskInfo> {
        return tasks.values.toList()
    }

    /**
     * 获取指定状态的任务
     */
    fun getTasksByStatus(status: Status): List<TaskInfo> {
        return tasks.values.filter { it.status == status }
    }

    /**
     * 清除所有任务
     */
    suspend fun clearAllTasks() = withContext(Dispatchers.IO) {
        tasks.clear()
        saveTaskStatus()
    }

    /**
     * 清除所有任务
     */
    suspend fun clearAllStatus() = withContext(Dispatchers.IO) {
        val result = PlatformService.writeFile(SettingContext.STATUS_FILE, "")
        result

    }

    /**
     * 删除任务
     */
    suspend fun removeTask(id: String) = withContext(Dispatchers.IO) {
        tasks.remove(id)
        saveTaskStatus()
    }
}

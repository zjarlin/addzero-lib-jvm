package site.addzero.cli.biz.task

import kotlinx.coroutines.CoroutineScope

/**
 * 抽象任务接口
 *
 * 所有具体任务都需要实现此接口，提供任务描述和执行逻辑
 */
interface AbsTask {

//  val statusManager:  TaskStatusService by inject()
    /** 任务描述 */
    val des: String

    /** 任务状态 */
    val status: Status
        get() = Status.PENDING

    /** 任务名称 */
    val taskName: String get() = this::class.java.simpleName

    /**
     * 生成任务ID
     *
     * @return 任务ID
     */
    fun generateTaskId(): String {
        return System.currentTimeMillis().toString() + "-" + (0..1000000).random().toString()
    }

    /**
     * 执行任务
     *
     * @param scope 协程作用域
     * @param taskId 任务ID
     */
    suspend fun execute(scope: CoroutineScope, taskId: String) {
        val taskInfo = TaskInfo(taskId, taskName, des)
        try {
            taskStatusService.initStatusDir()
            taskStatusService.addTask(taskInfo)
            taskStatusService.updateTaskStatus(taskId, Status.RUNNING)
            // 执行具体任务逻辑
            executeInternal(scope)
            taskStatusService.updateTaskStatus(taskId, Status.COMPLETED)
        } catch (e: Exception) {
            taskStatusService.updateTaskStatus(taskId, Status.FAILED, e.message)
            handleException(e)
        }
    }

    /**
     * 执行具体任务逻辑，由实现类提供
     *
     * @param scope 协程作用域
     */
    suspend fun executeInternal(scope: CoroutineScope)

    /**
     * 处理任务执行过程中的异常
     *
     * @param e 异常对象
     */
    fun handleException(e: Exception) {
        println("任务执行失败: ${e.message}")
    }
}

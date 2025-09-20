package site.addzero.cli.commands.repl

import org.koin.core.annotation.Single
import site.addzero.app.AdvancedRepl
import site.addzero.app.ParamDef
import site.addzero.cli.biz.task.Status
import site.addzero.cli.biz.task.taskStatusService

/**
 * TaskStatusCommand的REPL包装类，实现AdvancedRepl接口
 */
@Single

class TaskStatusCommandRepl : AdvancedRepl<Unit, String> {
    override val command: String = "cat-status"
    override val description: String = "查看任务执行状态"
    override val paramDefs
        get() = emptyList<ParamDef>()



    override fun eval(params: Unit): String {
           val tasks = taskStatusService.getAllTasks()
        val taskStatuses = tasks.joinToString(System.lineSeparator()) {
            val statusText = when (it.status) {
                Status.PENDING -> "待执行"
                Status.RUNNING -> "执行中"
                Status.COMPLETED -> "已完成"
                Status.FAILED -> "失败"
            }
            "${it.description} - $statusText"
        }
        if (taskStatuses.isBlank()) {
            System.err.println("没有任务正在执行")
        }
      return  taskStatuses
    }

    override fun createParams(values: List<Any?>) {
        return
    }
}

//package site.addzero.cli.tasks
//
//import site.addzero.cli.annotations.Task
//import site.addzero.cli.config.ConfigManager
//import site.addzero.cli.i18n.LanguageManager
//
//class TaskExecutor(private val configManager: ConfigManager) {
//    fun executeTask(obj: Any, methodName: String, vararg args: Any?): Any? {
//        val method = obj.javaClass.methods.find { it.name == methodName }
//            ?: throw IllegalArgumentException("Method $methodName not found")
//
//        // Check if method is annotated with @Task
//        val taskAnnotation = method.annotations.find { it.annotationClass == Task::class }
//        if (taskAnnotation != null) {
//            val taskId = (taskAnnotation as Task).id
//
//            // Check if task was already completed
//            if (configManager.isTaskCompleted(taskId)) {
//                println(LanguageManager.getMessage("task_executor", "task_already_completed", taskId))
//                return null
//            }
//
//            // Execute the task
//            val result = method.invoke(obj, *args)
//
//            // Mark task as completed
//            configManager.saveTaskStatus(taskId, true)
//
//            return result
//        }
//
//        // If not annotated, just execute the method
//        return method.invoke(obj, *args)
//    }
//}

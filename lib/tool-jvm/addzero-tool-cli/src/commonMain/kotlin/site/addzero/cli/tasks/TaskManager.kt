//package site.addzero.cli.tasks
//
//import site.addzero.cli.annotations.Task
//import site.addzero.cli.os.OperatingSystem
//import site.addzero.cli.config.DotfilesConfig
//import site.addzero.cli.config.PackageManagerConfig
//import site.addzero.cli.config.EnvironmentConfig
//import site.addzero.cli.i18n.LanguageManager
//import java.util.Scanner
//
//class TaskManager(
//    private val os: OperatingSystem,
//    private val config: DotfilesConfig,
//    private val taskExecutor: TaskExecutor
//) {
//    private val tasks = mutableListOf<TaskInfo>()
//
//    init {
//        initializeTasks()
//    }
//
//    private fun initializeTasks() {
//        // Initialize all available tasks with auto-generated numbers
//        tasks.clear()
//
//        // Init tasks
//        val initTask = InitTask(os)
//        tasks.add(TaskInfo(1, "setup_package_manager", LanguageManager.getMessage("tasks", "setup_package_manager"), initTask, "setupPackageManager", arrayOf(config.packageManager)))
//        tasks.add(TaskInfo(2, "setup_environment", LanguageManager.getMessage("tasks", "setup_environment"), initTask, "setupEnvironment", arrayOf(EnvironmentConfig())))
//
//        // Install tasks
//        val installTask = InstallTask(os)
//        val packageIds = if (config.packages.isNotEmpty()) {
//            config.packages
//        } else {
//            listOf("git", "vim", "curl", "wget")
//        }
//        tasks.add(TaskInfo(3, "install_packages", LanguageManager.getMessage("tasks", "install_packages"), installTask, "installPackages", arrayOf(packageIds, config.packageManager)))
//
//        // Sync tasks
//        val syncTask = SyncTask(os)
//        val repoUrl = if (config.repositoryUrl.isNotEmpty()) {
//            config.repositoryUrl
//        } else {
//            "https://github.com/example/dotfiles.git"
//        }
//        val localDir = "${System.getProperty("user.home")}/.dotfiles"
//        tasks.add(TaskInfo(4, "clone_dotfiles", LanguageManager.getMessage("tasks", "clone_dotfiles"), syncTask, "cloneDotfiles", arrayOf(repoUrl, localDir)))
//
//        val dotfiles = if (config.dotfiles.isNotEmpty()) {
//            config.dotfiles
//        } else {
//            listOf(".bashrc", ".vimrc", ".gitconfig")
//        }
//        val targetDir = System.getProperty("user.home")
//        tasks.add(TaskInfo(5, "create_symlinks", LanguageManager.getMessage("tasks", "create_symlinks"), syncTask, "createSymlinks", arrayOf(localDir, targetDir, dotfiles)))
//    }
//
//    fun showTaskMenu() {
//        println("\n${LanguageManager.getMessage("app", "title")} - ${LanguageManager.getMessage("app", "running_on")} ${os.name}")
//        println(LanguageManager.getMessage("messages", "available_tasks"))
//        println("----------------------------------------")
//
//        tasks.forEach { task ->
//            println("${task.number}. ${task.description}")
//        }
//
//        println("----------------------------------------")
//        println(LanguageManager.getMessage("messages", "enter_task_number"))
//    }
//
//    fun executeTaskByNumber(number: Int): Boolean {
//        val task = tasks.find { it.number == number }
//        return if (task != null) {
//            try {
//                val result = taskExecutor.executeTask(task.instance, task.methodName, *task.args)
//                result as? Boolean ?: true
//            } catch (e: Exception) {
//                println("${LanguageManager.getMessage("messages", "task_execution_failed")} ${e.message}")
//                false
//            }
//        } else {
//            println(LanguageManager.getMessage("messages", "invalid_task_number"))
//            false
//        }
//    }
//
//    fun runInteractiveMode() {
//        val scanner = Scanner(System.`in`)
//
//        while (true) {
//            showTaskMenu()
//            print(LanguageManager.getMessage("messages", "prompt_task_number"))
//
//            try {
//                val input = scanner.nextLine().trim()
//                if (input.lowercase() == "quit" || input.lowercase() == "exit") {
//                    println(LanguageManager.getMessage("messages", "exiting"))
//                    break
//                }
//
//                val number = input.toIntOrNull()
//                if (number != null) {
//                    executeTaskByNumber(number)
//                    println() // Add a blank line for better readability
//                } else {
//                    println(LanguageManager.getMessage("messages", "invalid_input"))
//                }
//            } catch (e: Exception) {
//                println("${LanguageManager.getMessage("messages", "error_occurred")} ${e.message}")
//            }
//        }
//    }
//
//    data class TaskInfo(
//        val number: Int,
//        val id: String,
//        val description: String,
//        val instance: Any,
//        val methodName: String,
//        val args: Array<Any?>
//    ) {
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (javaClass != other?.javaClass) return false
//
//            other as TaskInfo
//
//            if (number != other.number) return false
//            if (id != other.id) return false
//            if (description != other.description) return false
//            if (instance != other.instance) return false
//            if (methodName != other.methodName) return false
//            if (!args.contentEquals(other.args)) return false
//
//            return true
//        }
//
//        override fun hashCode(): Int {
//            var result = number
//            result = 31 * result + id.hashCode()
//            result = 31 * result + description.hashCode()
//            result = 31 * result + instance.hashCode()
//            result = 31 * result + methodName.hashCode()
//            result = 31 * result + args.contentHashCode()
//            return result
//        }
//    }
//}

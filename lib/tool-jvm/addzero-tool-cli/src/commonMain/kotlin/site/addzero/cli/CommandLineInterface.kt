//package site.addzero.cli
//
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//import site.addzero.cli.os.OperatingSystem
//import site.addzero.cli.config.ConfigManager
//import site.addzero.cli.config.ConfigLoader
//import site.addzero.cli.config.DotfilesConfig
//import site.addzero.cli.tasks.InitTask
//import site.addzero.cli.tasks.InstallTask
//import site.addzero.cli.tasks.SyncTask
//import site.addzero.cli.tasks.TaskExecutor
//import site.addzero.cli.tasks.TaskManager
//import site.addzero.cli.i18n.LanguageManager
//
//class CommandLineInterface : KoinComponent {
//    private val os: OperatingSystem by inject()
//    private val configManager: ConfigManager by inject()
//    private val configLoader: ConfigLoader by inject()
//    private val taskExecutor = TaskExecutor(configManager)
//    private var config: DotfilesConfig = DotfilesConfig()
//
//    init {
//        // Load configuration at startup
//        config = configLoader.loadConfig()
//    }
//
//    fun run(args: Array<String>) {
//        if (args.isEmpty()) {
//            // Run in interactive mode
//            val taskManager = TaskManager(os, config, taskExecutor)
//            taskManager.runInteractiveMode()
//            return
//        }
//
//        when (args[0]) {
//            "init" -> initialize()
//            "install" -> installPackages()
//            "sync" -> syncDotfiles()
//            "help" -> showHelp()
//            else -> println("${LanguageManager.getMessage("messages", "unknown_command")}${args[0]}")
//        }
//    }
//
//    private fun initialize() {
//        println(LanguageManager.getMessage("messages", "initializing"))
//        val initTask = InitTask(os)
//
//        // Setup package manager
//        val packageManagerConfig = config.packageManager
//        val success = taskExecutor.executeTask(initTask, "setupPackageManager", packageManagerConfig) as Boolean
//
//        if (success) {
//            println(LanguageManager.getMessage("messages", "initialization_completed"))
//        } else {
//            println(LanguageManager.getMessage("messages", "initialization_failed"))
//        }
//    }
//
//    private fun installPackages() {
//        println(LanguageManager.getMessage("messages", "installing_packages"))
//        val installTask = InstallTask(os)
//
//        // Use packages from config file, fallback to example list
//        val packageIds = if (config.packages.isNotEmpty()) {
//            config.packages
//        } else {
//            listOf("git", "vim", "curl", "wget")
//        }
//
//        val packageManagerConfig = config.packageManager
//
//        val success = taskExecutor.executeTask(installTask, "installPackages", packageIds, packageManagerConfig) as Boolean
//
//        if (success) {
//            println(LanguageManager.getMessage("messages", "package_installation_completed"))
//        } else {
//            println(LanguageManager.getMessage("messages", "package_installation_failed"))
//        }
//    }
//
//    private fun syncDotfiles() {
//        println(LanguageManager.getMessage("messages", "syncing_dotfiles"))
//        val syncTask = SyncTask(os)
//
//        // Use repository URL from config file, fallback to example URL
//        val repoUrl = if (config.repositoryUrl.isNotEmpty()) {
//            config.repositoryUrl
//        } else {
//            "https://github.com/example/dotfiles.git"
//        }
//
//        val localDir = "${System.getProperty("user.home")}/.dotfiles"
//
//        val cloneSuccess = taskExecutor.executeTask(syncTask, "cloneDotfiles", repoUrl, localDir) as Boolean
//
//        if (cloneSuccess) {
//            // Use dotfiles from config file, fallback to example list
//            val dotfiles = if (config.dotfiles.isNotEmpty()) {
//                config.dotfiles
//            } else {
//                listOf(".bashrc", ".vimrc", ".gitconfig")
//            }
//
//            val targetDir = System.getProperty("user.home")
//
//            val symlinkSuccess = taskExecutor.executeTask(syncTask, "createSymlinks", localDir, targetDir, dotfiles) as Boolean
//
//            if (symlinkSuccess) {
//                println(LanguageManager.getMessage("messages", "dotfiles_sync_completed"))
//            } else {
//                println(LanguageManager.getMessage("messages", "failed_create_symlinks"))
//            }
//        } else {
//            println(LanguageManager.getMessage("messages", "failed_clone_dotfiles"))
//        }
//    }
//
//    private fun showHelp() {
//        println("${LanguageManager.getMessage("app", "title")} - Help")
//        println(LanguageManager.getMessage("commands", "usage"))
//        println(LanguageManager.getMessage("commands", "available"))
//        println(LanguageManager.getMessage("commands", "init"))
//        println(LanguageManager.getMessage("commands", "install"))
//        println(LanguageManager.getMessage("commands", "sync"))
//        println(LanguageManager.getMessage("commands", "help"))
//        println("\n${LanguageManager.getMessage("messages", "config_file_location")}${System.getProperty("user.home")}/.config/dotfiles-cli/config.json")
//    }
//}

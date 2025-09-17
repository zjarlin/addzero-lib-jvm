//package site.addzero.cli.tasks
//
//import site.addzero.cli.annotations.CliCommand
//import site.addzero.cli.annotations.Task
//import site.addzero.cli.os.OperatingSystem
//import site.addzero.cli.dotfiles.DotfilesManager
//import site.addzero.cli.i18n.LanguageManager
//
//@CliCommand(name = "sync", description = "同步dotfiles配置文件")
//class SyncTask(private val os: OperatingSystem) {
//
//    @Task(id = "clone_dotfiles", description = "克隆dotfiles仓库")
//    fun cloneDotfiles(repoUrl: String, targetDir: String): Boolean {
//        println("${LanguageManager.getMessage("tasks", "clone_dotfiles")}...")
//        val dotfilesManager = DotfilesManager(os)
//        return dotfilesManager.cloneDotfiles(repoUrl, targetDir)
//    }
//
//    @Task(id = "create_symlinks", description = "为dotfiles创建符号链接")
//    fun createSymlinks(sourceDir: String, targetDir: String, files: List<String>): Boolean {
//        println("${LanguageManager.getMessage("tasks", "create_symlinks")}...")
//        val dotfilesManager = DotfilesManager(os)
//        return dotfilesManager.createSymlinks(sourceDir, targetDir, files)
//    }
//}

//package site.addzero.cli.tasks
//
//import site.addzero.cli.annotations.CliCommand
//import site.addzero.cli.annotations.Task
//import site.addzero.cli.os.OperatingSystem
//import site.addzero.cli.`package`.PackageManager
//import site.addzero.cli.config.PackageManagerConfig
//import site.addzero.cli.i18n.LanguageManager
//
//@CliCommand(name = "install", description = "安装软件包")
//class InstallTask(private val os: OperatingSystem) {
//
//    @Task(id = "install_packages", description = "使用系统包管理器安装软件包")
//    fun installPackages(packageIds: List<String>, config: PackageManagerConfig): Boolean {
//        println("${LanguageManager.getMessage("tasks", "install_packages")}...")
//        val packageManager = PackageManager(os, config)
//
//        return packageManager.installPackages(packageIds) { packageId, current, total ->
//            println("[$current/$total] ${LanguageManager.getMessage("package", "installing_with_command")} $packageId...")
//        }
//    }
//}

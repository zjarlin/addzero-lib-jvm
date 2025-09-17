//package site.addzero.cli.os
//
//import site.addzero.cli.annotations.OperatingSystemImplementation
//import site.addzero.cli.config.PackageManagerConfig
//import site.addzero.cli.i18n.LanguageManager
//import java.io.IOException
//
//@OperatingSystemImplementation(name = "Windows")
//class WindowsOS : OperatingSystem {
//    override val name: String = "Windows"
//    override val packageManager: String = "winget"
//    override val configFile: String = "${System.getProperty("user.home")}/.config/dotfiles-cli/config.json"
//
//    override fun detect(): Boolean {
//        val osName = System.getProperty("os.name").lowercase()
//        return osName.contains("windows")
//    }
//
//    override fun setupPackageManager(config: PackageManagerConfig): Boolean {
//        println("${LanguageManager.getMessage("os", "checking_for_winget")}...")
//        try {
//            val result = executeCommand("winget --version")
//            println("${LanguageManager.getMessage("os", "winget_found")}: $result")
//            return true
//        } catch (e: Exception) {
//            println("${LanguageManager.getMessage("os", "winget_not_found")}...")
//            return false
//        }
//    }
//
//    override fun createSymlink(source: String, target: String): Boolean {
//        try {
//            // On Windows, we use mklink command for symbolic links
//            val command = "cmd /c mklink /D \"$target\" \"$source\""
//            executeCommand(command)
//            return true
//        } catch (e: Exception) {
//            println("${LanguageManager.getMessage("os", "failed_create_symlink")}: ${e.message}")
//            return false
//        }
//    }
//
//    override fun executeCommand(command: String): String {
//        try {
//            val process = ProcessBuilder(*command.split(" ").toTypedArray()).start()
//            val output = process.inputStream.bufferedReader().readText()
//            process.waitFor()
//            return output
//        } catch (e: IOException) {
//            throw RuntimeException("Failed to execute command: $command", e)
//        }
//    }
//}

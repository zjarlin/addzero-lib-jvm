//package site.addzero.cli.os
//
//import site.addzero.cli.annotations.OperatingSystemImplementation
//import site.addzero.cli.config.PackageManagerConfig
//import site.addzero.cli.i18n.LanguageManager
//import java.io.IOException
//
//@OperatingSystemImplementation(name = "macOS")
//class MacOS : OperatingSystem {
//    override val name: String = "macOS"
//    override val packageManager: String = "brew"
//    override val configFile: String = "${System.getProperty("user.home")}/.config/dotfiles-cli/config.json"
//
//    override fun detect(): Boolean {
//        val osName = System.getProperty("os.name").lowercase()
//        return osName.contains("mac")
//    }
//
//    override fun setupPackageManager(config: PackageManagerConfig): Boolean {
//        println("${LanguageManager.getMessage("os", "checking_for_homebrew")}...")
//        try {
//            val result = executeCommand("brew --version")
//            println("${LanguageManager.getMessage("os", "homebrew_found")}: $result")
//            return true
//        } catch (e: Exception) {
//            println("${LanguageManager.getMessage("os", "homebrew_not_found_installing")}...")
//            try {
//                // Install Homebrew
//                val installCommand = "/bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
//                executeCommand(installCommand)
//                return true
//            } catch (installException: Exception) {
//                println("${LanguageManager.getMessage("os", "failed_install_homebrew")}: ${installException.message}")
//                return false
//            }
//        }
//    }
//
//    override fun createSymlink(source: String, target: String): Boolean {
//        try {
//            val command = "ln -sf \"$source\" \"$target\""
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

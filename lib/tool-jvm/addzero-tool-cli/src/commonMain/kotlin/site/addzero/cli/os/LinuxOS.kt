//package site.addzero.cli.os
//
//import site.addzero.cli.annotations.OperatingSystemImplementation
//import site.addzero.cli.config.PackageManagerConfig
//import site.addzero.cli.i18n.LanguageManager
//import java.io.IOException
//
//@OperatingSystemImplementation(name = "Linux")
//class LinuxOS : OperatingSystem {
//    override val name: String = "Linux"
//    override val packageManager: String = "apt" // Default, can be overridden
//    override val configFile: String = "${System.getProperty("user.home")}/.config/dotfiles-cli/config.json"
//
//    override fun detect(): Boolean {
//        val osName = System.getProperty("os.name").lowercase()
//        return osName.contains("linux")
//    }
//
//    override fun setupPackageManager(config: PackageManagerConfig): Boolean {
//        println("${LanguageManager.getMessage("os", "setting_up_package_manager")} Linux...")
//
//        // Use the package manager specified in config, or detect the default one
//        val pkgManager = if (config.packageManager.isNotEmpty()) {
//            config.packageManager
//        } else {
//            detectDefaultPackageManager()
//        }
//
//        println("${LanguageManager.getMessage("os", "using_package_manager")} $pkgManager")
//
//        try {
//            val result = executeCommand("$pkgManager --version")
//            println("$pkgManager ${LanguageManager.getMessage("os", "found")}: $result")
//            return true
//        } catch (e: Exception) {
//            println("$pkgManager ${LanguageManager.getMessage("os", "not_found_or_not_working")}.")
//            return false
//        }
//    }
//
//    private fun detectDefaultPackageManager(): String {
//        // Try to detect the default package manager
//        return try {
//            executeCommand("which apt >/dev/null 2>&1 && echo 'apt' || " +
//                          "(which yum >/dev/null 2>&1 && echo 'yum' || " +
//                          "(which pacman >/dev/null 2>&1 && echo 'pacman' || " +
//                          "(which dnf >/dev/null 2>&1 && echo 'dnf' || " +
//                          "(which zypper >/dev/null 2>&1 && echo 'zypper' || echo 'unknown'))))").trim()
//        } catch (e: Exception) {
//            "apt" // Default fallback
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

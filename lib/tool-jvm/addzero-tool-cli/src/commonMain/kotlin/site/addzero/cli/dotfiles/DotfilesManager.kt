//package site.addzero.cli.dotfiles
//
//import site.addzero.cli.os.OperatingSystem
//import site.addzero.cli.i18n.LanguageManager
//
//class DotfilesManager(private val os: OperatingSystem) {
//    fun cloneDotfiles(repoUrl: String, targetDir: String): Boolean {
//        println(LanguageManager.getMessage("dotfiles", "cloning_from_to", repoUrl, targetDir))
//
//        try {
//            // Create target directory if it doesn't exist
//            File(targetDir).mkdirs()
//
//            // Clone the repository
//            val command = "git clone $repoUrl $targetDir"
//            val result = os.executeCommand(command)
//            println(LanguageManager.getMessage("dotfiles", "clone_result", result))
//            return true
//        } catch (e: Exception) {
//            println(LanguageManager.getMessage("dotfiles", "failed_clone", e.message ?: ""))
//            return false
//        }
//    }
//
//    fun createSymlinks(sourceDir: String, targetDir: String, files: List<String>): Boolean {
//        println(LanguageManager.getMessage("dotfiles", "creating_symlinks_from_to", sourceDir, targetDir))
//
//        var successCount = 0
//        files.forEach { file ->
//            val sourcePath = "$sourceDir/$file"
//            val targetPath = "$targetDir/$file"
//
//            try {
//                // Create parent directory if it doesn't exist
//                File(targetPath).parentFile?.mkdirs()
//
//                // Create symlink
//                if (os.createSymlink(sourcePath, targetPath)) {
//                    println(LanguageManager.getMessage("dotfiles", "created_symlink_for", file))
//                    successCount++
//                }
//            } catch (e: Exception) {
//                println(LanguageManager.getMessage("dotfiles", "failed_create_symlink_for", file, e.message ?: ""))
//            }
//        }
//
//        return successCount == files.size
//    }
//}

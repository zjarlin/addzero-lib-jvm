package site.addzero.util

actual fun isMobile(): Boolean {
    return false
}

actual fun getPlatform(): Platform {
    return Platform.Jvm

}

actual object DirectoryLauncher {
    actual fun openDirectory(path: String): Boolean {
        val directory = java.io.File(path).absoluteFile
        if (!directory.exists()) {
            return false
        }

        return runCatching {
            when {
                java.awt.Desktop.isDesktopSupported() -> {
                    java.awt.Desktop.getDesktop().open(directory)
                }

                System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true) -> {
                    ProcessBuilder("open", directory.absolutePath).start()
                }

                System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true) -> {
                    ProcessBuilder("explorer", directory.absolutePath).start()
                }

                else -> {
                    ProcessBuilder("xdg-open", directory.absolutePath).start()
                }
            }
            true
        }.getOrDefault(false)
    }
}

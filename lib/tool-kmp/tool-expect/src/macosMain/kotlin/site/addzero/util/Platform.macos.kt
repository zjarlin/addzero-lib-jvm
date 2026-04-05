package site.addzero.util

actual fun isMobile(): Boolean {
    return false
}

actual fun getPlatform(): Platform {
   return Platform.Mac
}

actual object DirectoryLauncher {
    actual fun openDirectory(path: String): Boolean {
        return false
    }
}

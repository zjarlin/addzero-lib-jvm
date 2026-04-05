package site.addzero.util


actual fun isMobile(): Boolean {
    // iOS 设备通常是移动设备
    return true
}

actual fun getPlatform(): Platform = Platform.IOS

actual object DirectoryLauncher {
    actual fun openDirectory(path: String): Boolean {
        return false
    }
}

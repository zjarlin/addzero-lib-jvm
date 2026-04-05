@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package site.addzero.util

actual fun isMobile(): Boolean {
    return false
}

actual fun getPlatform(): Platform {
    return Platform.Web
}

actual object DirectoryLauncher {
    actual fun openDirectory(path: String): Boolean {
        return runCatching {
            openBrowserDirectory(path)
        }.getOrDefault(false)
    }
}

@JsFun("(path) => { globalThis.window?.open(path, '_blank'); return true; }")
private external fun openBrowserDirectory(path: String): Boolean

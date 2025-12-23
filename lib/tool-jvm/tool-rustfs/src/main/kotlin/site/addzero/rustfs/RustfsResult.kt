package site.addzero.rustfs

sealed class RustfsResult {
    data class Success(val message: String) : RustfsResult()
    data class Error(val message: String, val cause: Throwable? = null) : RustfsResult()
}

package site.addzero.core.network

internal expect inline fun <T> synchronized(
    lock: Any,
    block: () -> T,
): T

package site.addzero.core.network

internal actual inline fun <T> synchronized(
    lock: Any,
    block: () -> T,
): T = block()

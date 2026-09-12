package site.addzero.core.network

internal actual fun <T> withHttpClientFactoryLock(
  lock: Any,
  block: () -> T,
): T = synchronized(lock, block)

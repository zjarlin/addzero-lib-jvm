package site.addzero.biz.spec.iot.protocol.s7

import site.addzero.biz.spec.iot.requireText

/**
 * Connection parameters for a Siemens S7 PLC.
 */
class S7ConnectionOptions @JvmOverloads constructor(
    connectionId: String?,
    host: String?,
    val port: Int,
    threadId: String? = "0",
) {

    val connectionId: String = requireText(connectionId, "connectionId")
    val host: String = requireText(host, "host")
    val threadId: String = threadId?.trim()?.takeIf { it.isNotEmpty() } ?: "0"

    internal fun cacheKey(): String {
        return "$connectionId:$threadId"
    }
}

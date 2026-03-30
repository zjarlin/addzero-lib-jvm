package site.addzero.modbus.tcp.server

/**
 * Modbus TCP 服务端配置。
 */
data class ModbusTcpServerConfig(
    val host: String? = null,
    val port: Int = 502,
    val workerPoolSize: Int = 2,
    val defaultUnitId: Int = 1,
    val useRtuOverTcp: Boolean = false,
    val maxIdleSeconds: Int = 0,
) {
    init {
        require(host == null || host.isNotBlank()) {
            "host 为空字符串时请直接传 null"
        }
        require(port in 1..65535) {
            "port 必须在 1..65535"
        }
        require(workerPoolSize > 0) {
            "workerPoolSize 必须大于 0"
        }
        require(defaultUnitId in 0..255) {
            "defaultUnitId 必须在 0..255"
        }
        require(maxIdleSeconds >= 0) {
            "maxIdleSeconds 不能小于 0"
        }
    }
}

package site.addzero.modbus.tcp.client

/**
 * Modbus TCP 客户端配置。
 */
data class ModbusTcpClientConfig(
    val host: String,
    val port: Int = 502,
    val unitId: Int = 1,
    val timeoutMs: Int = 1_000,
    val reconnectPerRequest: Boolean = false,
    val useRtuOverTcp: Boolean = false,
) {
    init {
        require(host.isNotBlank()) {
            "host 不能为空"
        }
        require(port in 1..65535) {
            "port 必须在 1..65535"
        }
        require(unitId in 0..255) {
            "unitId 必须在 0..255"
        }
        require(timeoutMs >= 0) {
            "timeoutMs 不能小于 0"
        }
    }
}

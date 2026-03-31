package site.addzero.modbus.tcp.client

/**
 * Modbus TCP 客户端配置。
 */
data class ModbusTcpClientConfig(
    /**
     * 目标 Modbus TCP 服务端地址。
     */
    val host: String,
    /**
     * 目标端口，标准值通常是 502。
     */
    val port: Int = 502,
    /**
     * 目标从站地址。
     */
    val unitId: Int = 1,
    /**
     * 单次请求超时，单位毫秒。
     */
    val timeoutMs: Int = 1_000,
    /**
     * 是否每次请求前都重新连接。
     *
     * 适合链路不稳定、空闲连接容易被对端回收的场景。
     */
    val reconnectPerRequest: Boolean = false,
    /**
     * 是否启用 RTU over TCP 兼容模式。
     */
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

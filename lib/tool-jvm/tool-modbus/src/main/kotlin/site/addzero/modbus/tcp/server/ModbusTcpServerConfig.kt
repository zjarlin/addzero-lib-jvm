package site.addzero.modbus.tcp.server

/**
 * Modbus TCP 服务端配置。
 */
data class ModbusTcpServerConfig(
    /**
     * 绑定地址；传 `null` 表示监听本机所有网卡。
     */
    val host: String? = null,
    /**
     * 监听端口。
     */
    val port: Int = 502,
    /**
     * j2mod 内部处理请求的工作线程数。
     */
    val workerPoolSize: Int = 2,
    /**
     * 默认自动创建的 unit id。
     */
    val defaultUnitId: Int = 1,
    /**
     * 是否按 RTU over TCP 方式处理报文。
     */
    val useRtuOverTcp: Boolean = false,
    /**
     * 空闲连接最大保持时长，单位秒。
     */
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

package site.addzero.modbus.rtu.client

import site.addzero.serial.SerialPortConfig

/**
 * Modbus RTU 客户端配置。
 *
 * 串口本身的参数全部复用 [SerialPortConfig]，
 * 这里额外补充的只是 Modbus 协议层需要的内容。
 */
data class ModbusRtuClientConfig(
    /**
     * 串口层参数，直接决定 RTU 是否能和设备正常通信。
     */
    val serialConfig: SerialPortConfig,
    /**
     * 目标从站地址。
     */
    val unitId: Int = 1,
    /**
     * 单次 Modbus 请求超时，单位毫秒。
     */
    val requestTimeoutMs: Int = serialConfig.readTimeoutMs,
    /**
     * 失败后额外重试次数。
     *
     * 例如值为 1 时，总共会尝试 2 次。
     */
    val retries: Int = 0,
    /**
     * 是否每次请求都重新打开一个 RTU 会话。
     */
    val reconnectPerRequest: Boolean = false,
    /**
     * 帧间发送延迟，单位毫秒。
     *
     * 传负数表示交给 j2mod 使用默认行为。
     */
    val transmitDelayMs: Int = -1,
) {
    init {
        require(unitId in 0..255) {
            "unitId 必须在 0..255"
        }
        require(requestTimeoutMs >= 0) {
            "requestTimeoutMs 不能小于 0"
        }
        require(retries >= 0) {
            "retries 不能小于 0"
        }
    }
}

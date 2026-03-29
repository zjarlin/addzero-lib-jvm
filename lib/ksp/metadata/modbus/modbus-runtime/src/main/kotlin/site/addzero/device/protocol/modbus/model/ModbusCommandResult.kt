package site.addzero.device.protocol.modbus.model

import kotlinx.serialization.Serializable

/**
 * 通用命令执行结果。
 */
@Serializable
data class ModbusCommandResult(
    val accepted: Boolean,
    val summary: String,
)

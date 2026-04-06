package site.addzero.device.protocol.modbus.model

import kotlinx.serialization.Serializable

@Serializable
data class ModbusCommandResult(
    val accepted: Boolean,
    val summary: String,
)

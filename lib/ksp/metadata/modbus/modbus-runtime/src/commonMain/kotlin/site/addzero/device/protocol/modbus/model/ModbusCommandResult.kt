package site.addzero.device.protocol.modbus.model

import kotlinx.serialization.Serializable

@Serializable
data class ModbusCommandResult(
    val accepted: Boolean,
    val summary: String,
    val functionCode: Int? = null,
    val exceptionCode: Int? = null,
    val exceptionName: String? = null,
)

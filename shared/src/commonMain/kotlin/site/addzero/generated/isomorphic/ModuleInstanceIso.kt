package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity

@Serializable
data class ModuleInstanceIso(
    val id: Long = 0L,
    val name: String = "",
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: Parity? = null,
    val responseTimeoutMs: Int? = null,
    val sortIndex: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val protocol: ProtocolInstanceIso = ProtocolInstanceIso(),
    val moduleTemplate: ModuleTemplateIso = ModuleTemplateIso(),
    val devices: List<DeviceIso> = emptyList()
)
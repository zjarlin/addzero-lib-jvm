package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable

@Serializable
data class ProjectIso(
    val id: Long = 0L,
    val name: String = "",
    val description: String? = null,
    val remark: String? = null,
    val sortIndex: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val protocolLinks: List<ProjectProtocolIso> = emptyList(),
    val protocols: List<ProtocolInstanceIso> = emptyList(),
    val mqttConfig: ProjectMqttConfigIso? = null,
    val modbusServerConfigs: List<ProjectModbusServerConfigIso> = emptyList()
)
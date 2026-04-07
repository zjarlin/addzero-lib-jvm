package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable

@Serializable
data class ProtocolInstanceIso(
    val id: Long = 0L,
    val name: String = "",
    val pollingIntervalMs: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val protocolTemplate: ProtocolTemplateIso = ProtocolTemplateIso(),
    val projectLinks: List<ProjectProtocolIso> = emptyList(),
    val projects: List<ProjectIso> = emptyList(),
    val modules: List<ModuleInstanceIso> = emptyList()
)
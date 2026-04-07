package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable

@Serializable
data class ProjectProtocolIso(
    val id: Long = 0L,
    val sortIndex: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val project: ProjectIso = ProjectIso(),
    val protocol: ProtocolInstanceIso = ProtocolInstanceIso()
)
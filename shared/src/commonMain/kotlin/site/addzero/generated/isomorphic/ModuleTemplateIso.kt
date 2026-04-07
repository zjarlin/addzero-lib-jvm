package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable

@Serializable
data class ModuleTemplateIso(
    val id: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val sortIndex: Int = 0,
    val channelCount: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val protocolTemplate: ProtocolTemplateIso = ProtocolTemplateIso()
)
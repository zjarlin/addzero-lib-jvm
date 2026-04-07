package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable

@Serializable
data class ProtocolTemplateIso(
    val id: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val sortIndex: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val moduleTemplates: List<ModuleTemplateIso> = emptyList()
)
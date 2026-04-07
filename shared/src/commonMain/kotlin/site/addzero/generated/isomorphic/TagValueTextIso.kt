package site.addzero.generated.isomorphic

import kotlinx.serialization.Serializable

@Serializable
data class TagValueTextIso(
    val id: Long = 0L,
    val rawValue: String = "",
    val displayText: String = "",
    val sortIndex: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val tag: TagIso = TagIso()
)
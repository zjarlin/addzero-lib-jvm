// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoCharacterResource(
    val id: String?,
    val name: String?,
    /**
     * Unix timestamp (in seconds) when the character was created.
     */
    @SerialName("created_at")
    val createdAt: Long
)

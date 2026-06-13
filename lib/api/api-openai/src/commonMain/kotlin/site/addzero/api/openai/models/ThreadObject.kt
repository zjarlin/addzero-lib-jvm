// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a thread that contains [messages](/docs/api-reference/messages).
 */
@Serializable
data class ThreadObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `thread`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the thread was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("tool_resources")
    val toolResources: site.addzero.api.openai.models.ThreadObjectToolResources?,
    val metadata: site.addzero.api.openai.models.Metadata?
)

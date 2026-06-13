// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The compacted response object
 */
@Serializable
data class CompactResource(
    /**
     * The unique identifier for the compacted response.
     */
    val id: String,
    /**
     * The object type. Always `response.compaction`.
     */
    @SerialName("object")
    val objectType: String = "response.compaction",
    /**
     * The compacted list of output items.
     */
    val output: List<site.addzero.api.openai.models.ItemField>,
    /**
     * Unix timestamp (in seconds) when the compacted conversation was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Token accounting for the compaction pass, including cached, reasoning, and total tokens.
     */
    val usage: site.addzero.api.openai.models.ResponseUsage
)

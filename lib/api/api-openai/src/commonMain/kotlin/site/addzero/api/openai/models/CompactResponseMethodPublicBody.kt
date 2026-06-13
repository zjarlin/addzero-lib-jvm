// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CompactResponseMethodPublicBody(
    val model: site.addzero.api.openai.models.ModelIdsCompaction?,
    val input: JsonElement? = null,
    @SerialName("previous_response_id")
    val previousResponseId: String? = null,
    val instructions: String? = null,
    @SerialName("prompt_cache_key")
    val promptCacheKey: String? = null,
    @SerialName("prompt_cache_retention")
    val promptCacheRetention: site.addzero.api.openai.models.PromptCacheRetentionEnum? = null,
    @SerialName("service_tier")
    val serviceTier: site.addzero.api.openai.models.ServiceTierEnum? = null
)

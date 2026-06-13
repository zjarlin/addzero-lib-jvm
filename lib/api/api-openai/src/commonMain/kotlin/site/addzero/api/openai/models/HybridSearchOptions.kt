// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HybridSearchOptions(
    /**
     * The weight of the embedding in the reciprocal ranking fusion.
     */
    @SerialName("embedding_weight")
    val embeddingWeight: Double,
    /**
     * The weight of the text in the reciprocal ranking fusion.
     */
    @SerialName("text_weight")
    val textWeight: Double
)

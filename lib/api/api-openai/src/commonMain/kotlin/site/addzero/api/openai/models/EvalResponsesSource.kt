// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A EvalResponsesSource object describing a run data source configuration.
 */
@Serializable
data class EvalResponsesSource(
    /**
     * The type of run data source. Always `responses`.
     */
    val type: String,
    val metadata: Map<String, JsonElement>? = null,
    val model: String? = null,
    @SerialName("instructions_search")
    val instructionsSearch: String? = null,
    @SerialName("created_after")
    val createdAfter: Int? = null,
    @SerialName("created_before")
    val createdBefore: Int? = null,
    @SerialName("reasoning_effort")
    val reasoningEffort: site.addzero.api.openai.models.ReasoningEffort? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    val users: List<String>? = null,
    val tools: List<String>? = null
)

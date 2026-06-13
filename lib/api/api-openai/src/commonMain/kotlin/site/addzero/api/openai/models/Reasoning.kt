// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **gpt-5 and o-series models only** Configuration options for [reasoning
 * models](https://platform.openai.com/docs/guides/reasoning).
 */
@Serializable
data class Reasoning(
    val effort: site.addzero.api.openai.models.ReasoningEffort? = null,
    val summary: String? = null,
    @SerialName("generate_summary")
    val generateSummary: String? = null
)

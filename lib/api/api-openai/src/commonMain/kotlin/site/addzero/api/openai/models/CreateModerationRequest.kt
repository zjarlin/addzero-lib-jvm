// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateModerationRequest(
    /**
     * Input (or inputs) to classify. Can be a single string, an array of strings, or an array of multi-
     * modal input objects similar to other models.
     */
    val input: JsonElement,
    /**
     * The content moderation model you would like to use. Learn more in [the moderation
     * guide](/docs/guides/moderation), and learn about available models [here](/docs/models#moderation).
     */
    val model: String? = "omni-moderation-latest"
)

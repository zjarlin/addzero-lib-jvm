// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Static predicted output content, such as the content of a text file that is being regenerated.
 */
@Serializable
data class PredictionContent(
    /**
     * The type of the predicted content you want to provide. This type is currently always `content`.
     */
    val type: String,
    /**
     * The content that should be matched when generating a model response. If generated tokens would match
     * this content, the entire model response can be returned much more quickly.
     */
    val content: JsonElement
)

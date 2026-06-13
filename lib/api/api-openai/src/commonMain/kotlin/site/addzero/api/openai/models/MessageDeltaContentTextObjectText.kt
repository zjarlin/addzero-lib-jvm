// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MessageDeltaContentTextObjectText(
    /**
     * The data that makes up the text.
     */
    val value: String? = null,
    val annotations: List<JsonElement>? = null
)

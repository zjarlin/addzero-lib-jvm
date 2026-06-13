// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes an OpenAI model offering that can be used with the API.
 */
@Serializable
data class Model(
    /**
     * The model identifier, which can be referenced in the API endpoints.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) when the model was created.
     */
    val created: Long,
    /**
     * The object type, which is always "model".
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The organization that owns the model.
     */
    @SerialName("owned_by")
    val ownedBy: String
)

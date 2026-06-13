// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A refusal from the model.
 */
@Serializable
data class RefusalContent(
    /**
     * The type of the refusal. Always `refusal`.
     */
    val type: String = "refusal",
    /**
     * The refusal explanation from the model.
     */
    val refusal: String
)

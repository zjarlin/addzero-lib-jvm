// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An object describing text to classify.
 */
@Serializable
data class CreateModerationRequestInputItem2(
    /**
     * Always `text`.
     */
    val type: String,
    /**
     * A string of text to classify.
     */
    val text: String
)

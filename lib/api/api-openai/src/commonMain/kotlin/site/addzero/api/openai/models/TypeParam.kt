// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An action to type in text.
 */
@Serializable
data class TypeParam(
    /**
     * Specifies the event type. For a type action, this property is always set to `type`.
     */
    val type: String = "type",
    /**
     * The text to type.
     */
    val text: String
)

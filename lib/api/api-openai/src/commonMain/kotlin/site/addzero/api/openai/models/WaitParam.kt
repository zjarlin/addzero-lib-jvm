// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A wait action.
 */
@Serializable
data class WaitParam(
    /**
     * Specifies the event type. For a wait action, this property is always set to `wait`.
     */
    val type: String = "wait"
)

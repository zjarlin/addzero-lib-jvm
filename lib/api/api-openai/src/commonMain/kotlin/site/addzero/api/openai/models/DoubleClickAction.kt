// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A double click action.
 */
@Serializable
data class DoubleClickAction(
    /**
     * Specifies the event type. For a double click action, this property is always set to `double_click`.
     */
    val type: String = "double_click",
    /**
     * The x-coordinate where the double click occurred.
     */
    val x: Int,
    /**
     * The y-coordinate where the double click occurred.
     */
    val y: Int,
    val keys: List<String>?
)

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A mouse move action.
 */
@Serializable
data class MoveParam(
    /**
     * Specifies the event type. For a move action, this property is always set to `move`.
     */
    val type: String = "move",
    /**
     * The x-coordinate to move to.
     */
    val x: Int,
    /**
     * The y-coordinate to move to.
     */
    val y: Int,
    val keys: List<String>? = null
)

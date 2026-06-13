// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A drag action.
 */
@Serializable
data class DragParam(
    /**
     * Specifies the event type. For a drag action, this property is always set to `drag`.
     */
    val type: String = "drag",
    /**
     * An array of coordinates representing the path of the drag action. Coordinates will appear as an
     * array of objects, eg ``` [ { x: 100, y: 200 }, { x: 200, y: 300 } ] ```
     */
    val path: List<site.addzero.api.openai.models.CoordParam>,
    val keys: List<String>? = null
)

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An x/y coordinate pair, e.g. `{ x: 100, y: 200 }`.
 */
@Serializable
data class DragPoint(
    /**
     * The x-coordinate.
     */
    val x: Int,
    /**
     * The y-coordinate.
     */
    val y: Int
)

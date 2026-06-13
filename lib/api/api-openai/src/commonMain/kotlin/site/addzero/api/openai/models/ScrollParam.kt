// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A scroll action.
 */
@Serializable
data class ScrollParam(
    /**
     * Specifies the event type. For a scroll action, this property is always set to `scroll`.
     */
    val type: String = "scroll",
    /**
     * The x-coordinate where the scroll occurred.
     */
    val x: Int,
    /**
     * The y-coordinate where the scroll occurred.
     */
    val y: Int,
    /**
     * The horizontal scroll distance.
     */
    @SerialName("scroll_x")
    val scrollX: Int,
    /**
     * The vertical scroll distance.
     */
    @SerialName("scroll_y")
    val scrollY: Int,
    val keys: List<String>? = null
)

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A click action.
 */
@Serializable
data class ClickParam(
    /**
     * Specifies the event type. For a click action, this property is always `click`.
     */
    val type: String = "click",
    /**
     * Indicates which mouse button was pressed during the click. One of `left`, `right`, `wheel`, `back`,
     * or `forward`.
     */
    val button: site.addzero.api.openai.models.ClickButtonType,
    /**
     * The x-coordinate where the click occurred.
     */
    val x: Int,
    /**
     * The y-coordinate where the click occurred.
     */
    val y: Int,
    val keys: List<String>? = null
)

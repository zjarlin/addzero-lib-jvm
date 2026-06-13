// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A collection of keypresses the model would like to perform.
 */
@Serializable
data class KeyPressAction(
    /**
     * Specifies the event type. For a keypress action, this property is always set to `keypress`.
     */
    val type: String = "keypress",
    /**
     * The combination of keys the model is requesting to be pressed. This is an array of strings, each
     * representing a key.
     */
    val keys: List<String>
)

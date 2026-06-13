// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Server-side semantic turn detection which uses a model to determine when the user has finished
 * speaking.
 */
@Serializable
data class RealtimeTurnDetection3(
    /**
     * Type of turn detection, `semantic_vad` to turn on Semantic VAD.
     */
    val type: String,
    /**
     * Used only for `semantic_vad` mode. The eagerness of the model to respond. `low` will wait longer for
     * the user to continue speaking, `high` will respond more quickly. `auto` is the default and is
     * equivalent to `medium`. `low`, `medium`, and `high` have max timeouts of 8s, 4s, and 2s
     * respectively.
     */
    val eagerness: String? = "auto",
    /**
     * Whether or not to automatically generate a response when a VAD stop event occurs.
     */
    @SerialName("create_response")
    val createResponse: Boolean? = true,
    /**
     * Whether or not to automatically interrupt any ongoing response with output to the default
     * conversation (i.e. `conversation` of `auto`) when a VAD start event occurs.
     */
    @SerialName("interrupt_response")
    val interruptResponse: Boolean? = true
)

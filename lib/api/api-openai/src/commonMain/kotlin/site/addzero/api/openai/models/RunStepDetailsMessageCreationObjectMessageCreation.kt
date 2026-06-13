// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunStepDetailsMessageCreationObjectMessageCreation(
    /**
     * The ID of the message that was created by this run step.
     */
    @SerialName("message_id")
    val messageId: String
)

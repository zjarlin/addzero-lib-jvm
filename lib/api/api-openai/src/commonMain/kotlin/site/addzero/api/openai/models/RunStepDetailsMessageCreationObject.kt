// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details of the message creation by the run step.
 */
@Serializable
data class RunStepDetailsMessageCreationObject(
    /**
     * Always `message_creation`.
     */
    val type: String,
    @SerialName("message_creation")
    val messageCreation: site.addzero.api.openai.models.RunStepDetailsMessageCreationObjectMessageCreation
)

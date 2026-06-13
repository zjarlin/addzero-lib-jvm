// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The delta containing the fields that have changed on the run step.
 */
@Serializable
data class RunStepDeltaObjectDelta(
    /**
     * The details of the run step.
     */
    @SerialName("step_details")
    val stepDetails: JsonElement? = null
)

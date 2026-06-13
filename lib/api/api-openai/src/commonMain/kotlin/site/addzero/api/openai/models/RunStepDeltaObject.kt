// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a run step delta i.e. any changed fields on a run step during streaming.
 */
@Serializable
data class RunStepDeltaObject(
    /**
     * The identifier of the run step, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `thread.run.step.delta`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The delta containing the fields that have changed on the run step.
     */
    val delta: site.addzero.api.openai.models.RunStepDeltaObjectDelta
)

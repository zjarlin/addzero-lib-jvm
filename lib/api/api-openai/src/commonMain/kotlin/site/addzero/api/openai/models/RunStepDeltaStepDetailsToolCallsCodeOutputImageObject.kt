// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Code interpreter image output
 */
@Serializable
data class RunStepDeltaStepDetailsToolCallsCodeOutputImageObject(
    /**
     * The index of the output in the outputs array.
     */
    val index: Int,
    /**
     * Always `image`.
     */
    val type: String,
    val image: site.addzero.api.openai.models.RunStepDeltaStepDetailsToolCallsCodeOutputImageObjectImage? = null
)

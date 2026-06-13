// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Code Interpreter image output
 */
@Serializable
data class RunStepDetailsToolCallsCodeOutputImageObject(
    /**
     * Always `image`.
     */
    val type: String,
    val image: site.addzero.api.openai.models.RunStepDetailsToolCallsCodeOutputImageObjectImage
)

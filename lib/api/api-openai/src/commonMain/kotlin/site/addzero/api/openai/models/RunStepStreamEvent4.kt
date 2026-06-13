// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when parts of a [run step](/docs/api-reference/run-steps/step-object) are being streamed.
 */
@Serializable
data class RunStepStreamEvent4(
    val event: String,
    val data: site.addzero.api.openai.models.RunStepDeltaObject
)

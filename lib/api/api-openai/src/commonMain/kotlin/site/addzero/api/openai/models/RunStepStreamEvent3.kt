// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when a [run step](/docs/api-reference/run-steps/step-object) moves to an `in_progress` state.
 */
@Serializable
data class RunStepStreamEvent3(
    val event: String,
    val data: site.addzero.api.openai.models.RunStepObject
)

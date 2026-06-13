// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when a [run step](/docs/api-reference/run-steps/step-object) fails.
 */
@Serializable
data class RunStepStreamEvent6(
    val event: String,
    val data: site.addzero.api.openai.models.RunStepObject
)

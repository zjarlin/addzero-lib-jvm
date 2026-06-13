// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * ValidateGraderRequest
 */
@Serializable
data class ValidateGraderRequest(
    /**
     * The grader used for the fine-tuning job.
     */
    val grader: JsonElement
)

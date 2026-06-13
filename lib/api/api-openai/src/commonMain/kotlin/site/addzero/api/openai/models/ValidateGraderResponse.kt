// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * ValidateGraderResponse
 */
@Serializable
data class ValidateGraderResponse(
    /**
     * The grader used for the fine-tuning job.
     */
    val grader: JsonElement? = null
)

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * For fine-tuning jobs that have `failed`, this will contain more information on the cause of the
 * failure.
 */
@Serializable
data class FineTuningJobError(
    /**
     * A machine-readable error code.
     */
    val code: String,
    /**
     * A human-readable error message.
     */
    val message: String,
    val param: String?
)

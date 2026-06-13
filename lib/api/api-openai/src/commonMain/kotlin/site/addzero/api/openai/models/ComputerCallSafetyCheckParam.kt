// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A pending safety check for the computer call.
 */
@Serializable
data class ComputerCallSafetyCheckParam(
    /**
     * The ID of the pending safety check.
     */
    val id: String,
    val code: String? = null,
    val message: String? = null
)

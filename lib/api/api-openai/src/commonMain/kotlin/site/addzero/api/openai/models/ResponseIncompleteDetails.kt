// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Details about why the response is incomplete.
 */
@Serializable
data class ResponseIncompleteDetails(
    /**
     * The reason why the response is incomplete.
     */
    val reason: String? = null
)

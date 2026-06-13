// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Details on why the run is incomplete. Will be `null` if the run is not incomplete.
 */
@Serializable
data class RunObjectIncompleteDetails(
    /**
     * The reason why the run is incomplete. This will point to which specific token limit was reached over
     * the course of the run.
     */
    val reason: String? = null
)

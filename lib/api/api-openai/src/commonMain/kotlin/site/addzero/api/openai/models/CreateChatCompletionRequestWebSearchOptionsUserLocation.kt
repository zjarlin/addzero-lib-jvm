// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Approximate location parameters for the search.
 */
@Serializable
data class CreateChatCompletionRequestWebSearchOptionsUserLocation(
    /**
     * The type of location approximation. Always `approximate`.
     */
    val type: String,
    val approximate: site.addzero.api.openai.models.WebSearchLocation
)

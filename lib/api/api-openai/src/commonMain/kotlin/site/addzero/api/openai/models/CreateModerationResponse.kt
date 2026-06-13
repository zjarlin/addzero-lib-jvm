// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Represents if a given text input is potentially harmful.
 */
@Serializable
data class CreateModerationResponse(
    /**
     * The unique identifier for the moderation request.
     */
    val id: String,
    /**
     * The model used to generate the moderation results.
     */
    val model: String,
    /**
     * A list of moderation objects.
     */
    val results: List<site.addzero.api.openai.models.CreateModerationResponseResult>
)

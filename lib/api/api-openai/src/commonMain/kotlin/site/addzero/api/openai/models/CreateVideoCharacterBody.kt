// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Parameters for creating a character from an uploaded video.
 */
@Serializable
data class CreateVideoCharacterBody(
    /**
     * Video file used to create a character.
     */
    val video: ByteArray,
    /**
     * Display name for this API character.
     */
    val name: String
)

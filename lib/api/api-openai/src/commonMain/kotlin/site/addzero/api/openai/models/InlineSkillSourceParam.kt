// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Inline skill payload
 */
@Serializable
data class InlineSkillSourceParam(
    /**
     * The type of the inline skill source. Must be `base64`.
     */
    val type: String = "base64",
    /**
     * The media type of the inline skill payload. Must be `application/zip`.
     */
    @SerialName("media_type")
    val mediaType: String = "application/zip",
    /**
     * Base64-encoded skill zip bundle.
     */
    val data: String
)

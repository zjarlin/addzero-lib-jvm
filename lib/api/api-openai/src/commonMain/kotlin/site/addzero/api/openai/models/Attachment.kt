// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Attachment metadata included on thread items.
 */
@Serializable
data class Attachment(
    /**
     * Attachment discriminator.
     */
    val type: site.addzero.api.openai.models.AttachmentType,
    /**
     * Identifier for the attachment.
     */
    val id: String,
    /**
     * Original display name for the attachment.
     */
    val name: String,
    /**
     * MIME type of the attachment.
     */
    @SerialName("mime_type")
    val mimeType: String,
    @SerialName("preview_url")
    val previewUrl: String?
)

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `File` object represents a document that has been uploaded to OpenAI.
 */
@Serializable
data class OpenAIFile(
    /**
     * The file identifier, which can be referenced in the API endpoints.
     */
    val id: String,
    /**
     * The size of the file, in bytes.
     */
    val bytes: Int,
    /**
     * The Unix timestamp (in seconds) for when the file was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The Unix timestamp (in seconds) for when the file will expire.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    /**
     * The name of the file.
     */
    val filename: String,
    /**
     * The object type, which is always `file`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The intended purpose of the file. Supported values are `assistants`, `assistants_output`, `batch`,
     * `batch_output`, `fine-tune`, `fine-tune-results`, `vision`, and `user_data`.
     */
    val purpose: String,
    /**
     * Deprecated. The current status of the file, which can be either `uploaded`, `processed`, or `error`.
     */
    val status: String,
    /**
     * Deprecated. For details on why a fine-tuning training file failed validation, see the `error` field
     * on `fine_tuning.job`.
     */
    @SerialName("status_details")
    val statusDetails: String? = null
)

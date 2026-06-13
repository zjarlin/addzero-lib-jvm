// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFileRequest(
    /**
     * The File object (not file name) to be uploaded.
     */
    val file: ByteArray,
    /**
     * The intended purpose of the uploaded file. One of: - `assistants`: Used in the Assistants API -
     * `batch`: Used in the Batch API - `fine-tune`: Used for fine-tuning - `vision`: Images used for
     * vision fine-tuning - `user_data`: Flexible file type for any purpose - `evals`: Used for eval data
     * sets
     */
    val purpose: String,
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.FileExpirationAfter? = null
)

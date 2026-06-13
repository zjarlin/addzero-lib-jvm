// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateContainerFileBody(
    /**
     * Name of the file to create.
     */
    @SerialName("file_id")
    val fileId: String? = null,
    /**
     * The File object (not file name) to be uploaded.
     */
    val file: ByteArray? = null
)

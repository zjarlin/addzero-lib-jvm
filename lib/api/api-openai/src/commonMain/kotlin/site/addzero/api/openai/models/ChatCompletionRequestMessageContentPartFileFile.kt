// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestMessageContentPartFileFile(
    /**
     * The name of the file, used when passing the file to the model as a string.
     */
    val filename: String? = null,
    /**
     * The base64 encoded file data, used when passing the file to the model as a string.
     */
    @SerialName("file_data")
    val fileData: String? = null,
    /**
     * The ID of an uploaded file to use as input.
     */
    @SerialName("file_id")
    val fileId: String? = null
)

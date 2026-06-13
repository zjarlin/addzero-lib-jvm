// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A file input to the model.
 */
@Serializable
data class InputFileContentParam(
    /**
     * The type of the input item. Always `input_file`.
     */
    val type: String = "input_file",
    @SerialName("file_id")
    val fileId: String? = null,
    val filename: String? = null,
    @SerialName("file_data")
    val fileData: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    /**
     * The detail level of the file to be sent to the model. Use `low` for the default rendering behavior,
     * or `high` to render the file at higher quality. Defaults to `low`.
     */
    val detail: site.addzero.api.openai.models.FileDetailEnum? = null
)

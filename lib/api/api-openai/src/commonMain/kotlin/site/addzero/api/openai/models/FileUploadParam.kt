// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Controls whether users can upload files.
 */
@Serializable
data class FileUploadParam(
    /**
     * Enable uploads for this session. Defaults to false.
     */
    val enabled: Boolean? = null,
    /**
     * Maximum size in megabytes for each uploaded file. Defaults to 512 MB, which is the maximum allowable
     * size.
     */
    @SerialName("max_file_size")
    val maxFileSize: Int? = null,
    /**
     * Maximum number of files that can be uploaded to the session. Defaults to 10.
     */
    @SerialName("max_files")
    val maxFiles: Int? = null
)

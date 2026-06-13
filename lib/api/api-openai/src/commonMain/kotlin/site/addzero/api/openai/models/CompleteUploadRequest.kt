// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompleteUploadRequest(
    /**
     * The ordered list of Part IDs.
     */
    @SerialName("part_ids")
    val partIds: List<String>,
    /**
     * The optional md5 checksum for the file contents to verify if the bytes uploaded matches what you
     * expect.
     */
    val md5: String? = null
)

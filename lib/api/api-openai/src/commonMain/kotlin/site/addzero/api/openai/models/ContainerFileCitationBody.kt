// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A citation for a container file used to generate a model response.
 */
@Serializable
data class ContainerFileCitationBody(
    /**
     * The type of the container file citation. Always `container_file_citation`.
     */
    val type: String = "container_file_citation",
    /**
     * The ID of the container file.
     */
    @SerialName("container_id")
    val containerId: String,
    /**
     * The ID of the file.
     */
    @SerialName("file_id")
    val fileId: String,
    /**
     * The index of the first character of the container file citation in the message.
     */
    @SerialName("start_index")
    val startIndex: Int,
    /**
     * The index of the last character of the container file citation in the message.
     */
    @SerialName("end_index")
    val endIndex: Int,
    /**
     * The filename of the container file cited.
     */
    val filename: String
)

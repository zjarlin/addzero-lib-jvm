// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContainerFileListResource(
    /**
     * The type of object returned, must be 'list'.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * A list of container files.
     */
    val data: List<site.addzero.api.openai.models.ContainerFileResource>,
    /**
     * The ID of the first file in the list.
     */
    @SerialName("first_id")
    val firstId: String,
    /**
     * The ID of the last file in the list.
     */
    @SerialName("last_id")
    val lastId: String,
    /**
     * Whether there are more files available.
     */
    @SerialName("has_more")
    val hasMore: Boolean
)

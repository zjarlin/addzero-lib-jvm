// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The container file object
 */
@Serializable
data class ContainerFileResource(
    /**
     * Unique identifier for the file.
     */
    val id: String,
    /**
     * The type of this object (`container.file`).
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The container this file belongs to.
     */
    @SerialName("container_id")
    val containerId: String,
    /**
     * Unix timestamp (in seconds) when the file was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Size of the file in bytes.
     */
    val bytes: Int,
    /**
     * Path of the file in the container.
     */
    val path: String,
    /**
     * Source of the file (e.g., `user`, `assistant`).
     */
    val source: String
)

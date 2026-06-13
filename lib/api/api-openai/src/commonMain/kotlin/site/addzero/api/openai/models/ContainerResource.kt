// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The container object
 */
@Serializable
data class ContainerResource(
    /**
     * Unique identifier for the container.
     */
    val id: String,
    /**
     * The type of this object.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Name of the container.
     */
    val name: String,
    /**
     * Unix timestamp (in seconds) when the container was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Status of the container (e.g., active, deleted).
     */
    val status: String,
    /**
     * Unix timestamp (in seconds) when the container was last active.
     */
    @SerialName("last_active_at")
    val lastActiveAt: Long? = null,
    /**
     * The container will expire after this time period. The anchor is the reference point for the
     * expiration. The minutes is the number of minutes after the anchor before the container expires.
     */
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.ContainerResourceExpiresAfter? = null,
    /**
     * The memory limit configured for the container.
     */
    @SerialName("memory_limit")
    val memoryLimit: String? = null,
    /**
     * Network access policy for the container.
     */
    @SerialName("network_policy")
    val networkPolicy: site.addzero.api.openai.models.ContainerResourceNetworkPolicy? = null
)

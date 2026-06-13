// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The container will expire after this time period. The anchor is the reference point for the
 * expiration. The minutes is the number of minutes after the anchor before the container expires.
 */
@Serializable
data class ContainerResourceExpiresAfter(
    /**
     * The reference point for the expiration.
     */
    val anchor: String? = null,
    /**
     * The number of minutes after the anchor before the container expires.
     */
    val minutes: Int? = null
)

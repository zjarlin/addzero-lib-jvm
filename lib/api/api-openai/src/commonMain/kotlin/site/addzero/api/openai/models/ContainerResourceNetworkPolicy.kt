// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network access policy for the container.
 */
@Serializable
data class ContainerResourceNetworkPolicy(
    /**
     * The network policy mode.
     */
    val type: String,
    /**
     * Allowed outbound domains when `type` is `allowlist`.
     */
    @SerialName("allowed_domains")
    val allowedDomains: List<String>? = null
)

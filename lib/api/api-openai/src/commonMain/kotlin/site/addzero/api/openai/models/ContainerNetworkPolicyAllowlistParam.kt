// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContainerNetworkPolicyAllowlistParam(
    /**
     * Allow outbound network access only to specified domains. Always `allowlist`.
     */
    val type: String = "allowlist",
    /**
     * A list of allowed domains when type is `allowlist`.
     */
    @SerialName("allowed_domains")
    val allowedDomains: List<String>,
    /**
     * Optional domain-scoped secrets for allowlisted domains.
     */
    @SerialName("domain_secrets")
    val domainSecrets: List<site.addzero.api.openai.models.ContainerNetworkPolicyDomainSecretParam>? = null
)

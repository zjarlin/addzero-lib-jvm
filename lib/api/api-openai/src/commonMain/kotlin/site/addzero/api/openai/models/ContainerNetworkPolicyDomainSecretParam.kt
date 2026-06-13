// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ContainerNetworkPolicyDomainSecretParam(
    /**
     * The domain associated with the secret.
     */
    val domain: String,
    /**
     * The name of the secret to inject for the domain.
     */
    val name: String,
    /**
     * The secret value to inject for the domain.
     */
    val value: String
)

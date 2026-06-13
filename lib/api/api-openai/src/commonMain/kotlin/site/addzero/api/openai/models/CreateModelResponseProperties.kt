// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateModelResponseProperties(
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    @SerialName("top_logprobs")
    val topLogprobs: Int? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    /**
     * This field is being replaced by `safety_identifier` and `prompt_cache_key`. Use `prompt_cache_key`
     * instead to maintain caching optimizations. A stable identifier for your end-users. Used to boost
     * cache hit rates by better bucketing similar requests and to help OpenAI detect and prevent abuse.
     * [Learn more](/docs/guides/safety-best-practices#safety-identifiers).
     */
    val user: String? = null,
    /**
     * A stable identifier used to help detect users of your application that may be violating OpenAI's
     * usage policies. The IDs should be a string that uniquely identifies each user, with a maximum length
     * of 64 characters. We recommend hashing their username or email address, in order to avoid sending us
     * any identifying information. [Learn more](/docs/guides/safety-best-practices#safety-identifiers).
     */
    @SerialName("safety_identifier")
    val safetyIdentifier: String? = null,
    /**
     * Used by OpenAI to cache responses for similar requests to optimize your cache hit rates. Replaces
     * the `user` field. [Learn more](/docs/guides/prompt-caching).
     */
    @SerialName("prompt_cache_key")
    val promptCacheKey: String? = null,
    @SerialName("service_tier")
    val serviceTier: site.addzero.api.openai.models.ServiceTier? = null,
    @SerialName("prompt_cache_retention")
    val promptCacheRetention: String? = null
)

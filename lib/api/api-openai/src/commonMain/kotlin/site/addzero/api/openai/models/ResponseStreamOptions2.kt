// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Options for streaming responses. Only set this when you set `stream: true`.
 */
@Serializable
data class ResponseStreamOptions2(
    /**
     * When true, stream obfuscation will be enabled. Stream obfuscation adds random characters to an
     * `obfuscation` field on streaming delta events to normalize payload sizes as a mitigation to certain
     * side-channel attacks. These obfuscation fields are included by default, but add a small amount of
     * overhead to the data stream. You can set `include_obfuscation` to false to optimize for bandwidth if
     * you trust the network links between your application and the OpenAI API.
     */
    @SerialName("include_obfuscation")
    val includeObfuscation: Boolean? = null
)

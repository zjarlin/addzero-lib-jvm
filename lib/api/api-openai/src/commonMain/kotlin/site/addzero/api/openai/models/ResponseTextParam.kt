// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration options for a text response from the model. Can be plain text or structured JSON data.
 * Learn more: - [Text inputs and outputs](/docs/guides/text) - [Structured
 * Outputs](/docs/guides/structured-outputs)
 */
@Serializable
data class ResponseTextParam(
    val format: site.addzero.api.openai.models.TextResponseFormatConfiguration? = null,
    val verbosity: site.addzero.api.openai.models.Verbosity? = null
)

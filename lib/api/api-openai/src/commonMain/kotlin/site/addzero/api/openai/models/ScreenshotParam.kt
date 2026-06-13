// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A screenshot action.
 */
@Serializable
data class ScreenshotParam(
    /**
     * Specifies the event type. For a screenshot action, this property is always set to `screenshot`.
     */
    val type: String = "screenshot"
)

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunStepDeltaStepDetailsToolCallsCodeOutputImageObjectImage(
    /**
     * The [file](/docs/api-reference/files) ID of the image.
     */
    @SerialName("file_id")
    val fileId: String? = null
)

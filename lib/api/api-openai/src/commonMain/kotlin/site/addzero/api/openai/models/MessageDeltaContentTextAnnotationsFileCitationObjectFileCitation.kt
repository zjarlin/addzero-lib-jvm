// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDeltaContentTextAnnotationsFileCitationObjectFileCitation(
    /**
     * The ID of the specific File the citation is from.
     */
    @SerialName("file_id")
    val fileId: String? = null,
    /**
     * The specific quote in the file.
     */
    val quote: String? = null
)

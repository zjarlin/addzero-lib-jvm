// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBatchRequest(
    /**
     * The ID of an uploaded file that contains requests for the new batch. See [upload file](/docs/api-
     * reference/files/create) for how to upload a file. Your input file must be formatted as a [JSONL
     * file](/docs/api-reference/batch/request-input), and must be uploaded with the purpose `batch`. The
     * file can contain up to 50,000 requests, and can be up to 200 MB in size.
     */
    @SerialName("input_file_id")
    val inputFileId: String,
    /**
     * The endpoint to be used for all requests in the batch. Currently `/v1/responses`,
     * `/v1/chat/completions`, `/v1/embeddings`, `/v1/completions`, `/v1/moderations`,
     * `/v1/images/generations`, `/v1/images/edits`, and `/v1/videos` are supported. Note that
     * `/v1/embeddings` batches are also restricted to a maximum of 50,000 embedding inputs across all
     * requests in the batch.
     */
    val endpoint: String,
    /**
     * The time frame within which the batch should be processed. Currently only `24h` is supported.
     */
    @SerialName("completion_window")
    val completionWindow: String,
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    @SerialName("output_expires_after")
    val outputExpiresAfter: site.addzero.api.openai.models.BatchFileExpirationAfter? = null
)

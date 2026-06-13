// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateFileRequest
import site.addzero.api.openai.models.DeleteFileResponse
import site.addzero.api.openai.models.ListFilesResponse
import site.addzero.api.openai.models.OpenAIFile

interface OpenAiFilesApi {

    /**
     * Returns a list of files. REST: GET /files
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.FILES)
    suspend fun listFiles(
        @Query("purpose") purpose: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.ListFilesResponse

    /**
     * Upload a file that can be used across various endpoints. Individual files can be up to 512 MB, and
     * each project can store up to 2.5 TB of files in total. There is no organization-wide storage limit.
     * Uploads to this endpoint are rate-limited to 1,000 requests per minute per authenticated user. - The
     * Assistants API supports files up to 2 million tokens and of specific file types. See the [Assistants
     * Tools guide](/docs/assistants/tools) for details. - The Fine-tuning API only supports `.jsonl`
     * files. The input also has certain required formats for fine-tuning [chat](/docs/api-reference/fine-
     * tuning/chat-input) or [completions](/docs/api-reference/fine-tuning/completions-input) models. - The
     * Batch API only supports `.jsonl` files up to 200 MB in size. The input also has a specific required
     * [format](/docs/api-reference/batch/request-input). - For Retrieval or `file_search` ingestion,
     * upload files here first. If you need to attach multiple uploaded files to the same vector store, use
     * [`/vector_stores/{vector_store_id}/file_batches`](/docs/api-reference/vector-stores-file-
     * batches/createBatch) instead of attaching them one by one. Vector store attachment has separate
     * limits from file upload, including 2,000 attached files per minute per organization. Please [contact
     * us](https://help.openai.com/) if you need to increase these storage limits. REST: POST /files
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.FILES)
    suspend fun createFile(
        @Body body: site.addzero.api.openai.models.CreateFileRequest
    ): site.addzero.api.openai.models.OpenAIFile

    /**
     * Returns information about a specific file. REST: GET /files/{file_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.FILES_BY_FILE_ID)
    suspend fun retrieveFile(
        @Path("file_id") fileId: String
    ): site.addzero.api.openai.models.OpenAIFile

    /**
     * Delete a file and remove it from all vector stores. REST: DELETE /files/{file_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.FILES_BY_FILE_ID)
    suspend fun deleteFile(
        @Path("file_id") fileId: String
    ): site.addzero.api.openai.models.DeleteFileResponse

    /**
     * Returns the contents of the specified file. REST: GET /files/{file_id}/content
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.FILES_BY_FILE_ID_BY_CONTENT)
    suspend fun downloadFile(
        @Path("file_id") fileId: String
    ): String

}

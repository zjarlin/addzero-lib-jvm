// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.AddUploadPartRequest
import site.addzero.api.openai.models.CompleteUploadRequest
import site.addzero.api.openai.models.CreateUploadRequest
import site.addzero.api.openai.models.Upload
import site.addzero.api.openai.models.UploadPart

interface OpenAiUploadsApi {

    /**
     * Creates an intermediate [Upload](/docs/api-reference/uploads/object) object that you can add
     * [Parts](/docs/api-reference/uploads/part-object) to. Currently, an Upload can accept at most 8 GB in
     * total and expires after an hour after you create it. Once you complete the Upload, we will create a
     * [File](/docs/api-reference/files/object) object that contains all the parts you uploaded. This File
     * is usable in the rest of our platform as a regular File object. For certain `purpose` values, the
     * correct `mime_type` must be specified. Please refer to documentation for the [supported MIME types
     * for your use case](/docs/assistants/tools/file-search#supported-files). For guidance on the proper
     * filename extensions for each purpose, please follow the documentation on [creating a
     * File](/docs/api-reference/files/create). Returns the Upload object with status `pending`. REST: POST
     * /uploads
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.UPLOADS)
    suspend fun createUpload(
        @Body body: site.addzero.api.openai.models.CreateUploadRequest
    ): site.addzero.api.openai.models.Upload

    /**
     * Cancels the Upload. No Parts may be added after an Upload is cancelled. Returns the Upload object
     * with status `cancelled`. REST: POST /uploads/{upload_id}/cancel
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.UPLOADS_BY_UPLOAD_ID_BY_CANCEL)
    suspend fun cancelUpload(
        @Path("upload_id") uploadId: String
    ): site.addzero.api.openai.models.Upload

    /**
     * Completes the [Upload](/docs/api-reference/uploads/object). Within the returned Upload object, there
     * is a nested [File](/docs/api-reference/files/object) object that is ready to use in the rest of the
     * platform. You can specify the order of the Parts by passing in an ordered list of the Part IDs. The
     * number of bytes uploaded upon completion must match the number of bytes initially specified when
     * creating the Upload object. No Parts may be added after an Upload is completed. Returns the Upload
     * object with status `completed`, including an additional `file` property containing the created
     * usable File object. REST: POST /uploads/{upload_id}/complete
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.UPLOADS_BY_UPLOAD_ID_BY_COMPLETE)
    suspend fun completeUpload(
        @Path("upload_id") uploadId: String,
        @Body body: site.addzero.api.openai.models.CompleteUploadRequest
    ): site.addzero.api.openai.models.Upload

    /**
     * Adds a [Part](/docs/api-reference/uploads/part-object) to an [Upload](/docs/api-
     * reference/uploads/object) object. A Part represents a chunk of bytes from the file you are trying to
     * upload. Each Part can be at most 64 MB, and you can add Parts until you hit the Upload maximum of 8
     * GB. It is possible to add multiple Parts in parallel. You can decide the intended order of the Parts
     * when you [complete the Upload](/docs/api-reference/uploads/complete). REST: POST
     * /uploads/{upload_id}/parts
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.UPLOADS_BY_UPLOAD_ID_BY_PARTS)
    suspend fun addUploadPart(
        @Path("upload_id") uploadId: String,
        @Body body: site.addzero.api.openai.models.AddUploadPartRequest
    ): site.addzero.api.openai.models.UploadPart

}

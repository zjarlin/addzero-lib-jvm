// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Containers REST endpoints. */
interface OpenAiContainersApi {

    /**
     * List Containers
     *
     * REST: GET /containers
     */
    @GET(OpenAiApiPaths.CONTAINERS)
    suspend fun listContainers(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("name") name: String? = null
    ): OpenAiResponseBody

    /**
     * Create Container
     *
     * REST: POST /containers
     */
    @POST(OpenAiApiPaths.CONTAINERS)
    suspend fun createContainer(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Delete Container
     *
     * REST: DELETE /containers/{container_id}
     */
    @DELETE(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID)
    suspend fun deleteContainer(
        @Path("container_id") containerId: String
    ): OpenAiResponseBody

    /**
     * Retrieve Container
     *
     * REST: GET /containers/{container_id}
     */
    @GET(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID)
    suspend fun retrieveContainer(
        @Path("container_id") containerId: String
    ): OpenAiResponseBody

    /**
     * List Container files
     *
     * REST: GET /containers/{container_id}/files
     */
    @GET(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES)
    suspend fun listContainerFiles(
        @Path("container_id") containerId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Create a Container File You can send either a multipart/form-data request with the raw file content, or a JSON request with a file ID.
     *
     * REST: POST /containers/{container_id}/files
     */
    @POST(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES)
    suspend fun createContainerFile(
        @Path("container_id") containerId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete Container File
     *
     * REST: DELETE /containers/{container_id}/files/{file_id}
     */
    @DELETE(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES_BY_FILE_ID)
    suspend fun deleteContainerFile(
        @Path("container_id") containerId: String,
        @Path("file_id") fileId: String
    ): OpenAiResponseBody

    /**
     * Retrieve Container File
     *
     * REST: GET /containers/{container_id}/files/{file_id}
     */
    @GET(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES_BY_FILE_ID)
    suspend fun retrieveContainerFile(
        @Path("container_id") containerId: String,
        @Path("file_id") fileId: String
    ): OpenAiResponseBody

    /**
     * Retrieve Container File Content
     *
     * REST: GET /containers/{container_id}/files/{file_id}/content
     */
    @GET(OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES_BY_FILE_ID_BY_CONTENT)
    suspend fun retrieveContainerFileContent(
        @Path("container_id") containerId: String,
        @Path("file_id") fileId: String
    ): OpenAiBinaryBody
}

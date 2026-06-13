// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.ContainerFileListResource
import site.addzero.api.openai.models.ContainerFileResource
import site.addzero.api.openai.models.ContainerListResource
import site.addzero.api.openai.models.ContainerResource
import site.addzero.api.openai.models.CreateContainerBody
import site.addzero.api.openai.models.CreateContainerFileBody

interface OpenAiContainersApi {

    /**
     * List Containers REST: GET /containers
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS)
    suspend fun listContainers(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("name") name: String? = null
    ): site.addzero.api.openai.models.ContainerListResource

    /**
     * Create Container REST: POST /containers
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS)
    suspend fun createContainer(
        @Body body: site.addzero.api.openai.models.CreateContainerBody? = null
    ): site.addzero.api.openai.models.ContainerResource

    /**
     * Retrieve Container REST: GET /containers/{container_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID)
    suspend fun retrieveContainer(
        @Path("container_id") containerId: String
    ): site.addzero.api.openai.models.ContainerResource

    /**
     * Delete Container REST: DELETE /containers/{container_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID)
    suspend fun deleteContainer(
        @Path("container_id") containerId: String
    )

    /**
     * List Container files REST: GET /containers/{container_id}/files
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES)
    suspend fun listContainerFiles(
        @Path("container_id") containerId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.ContainerFileListResource

    /**
     * Create a Container File You can send either a multipart/form-data request with the raw file content,
     * or a JSON request with a file ID. REST: POST /containers/{container_id}/files
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES)
    suspend fun createContainerFile(
        @Path("container_id") containerId: String,
        @Body body: site.addzero.api.openai.models.CreateContainerFileBody
    ): site.addzero.api.openai.models.ContainerFileResource

    /**
     * Retrieve Container File REST: GET /containers/{container_id}/files/{file_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES_BY_FILE_ID)
    suspend fun retrieveContainerFile(
        @Path("container_id") containerId: String,
        @Path("file_id") fileId: String
    ): site.addzero.api.openai.models.ContainerFileResource

    /**
     * Delete Container File REST: DELETE /containers/{container_id}/files/{file_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES_BY_FILE_ID)
    suspend fun deleteContainerFile(
        @Path("container_id") containerId: String,
        @Path("file_id") fileId: String
    )

    /**
     * Retrieve Container File Content REST: GET /containers/{container_id}/files/{file_id}/content
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONTAINERS_BY_CONTAINER_ID_BY_FILES_BY_FILE_ID_BY_CONTENT)
    suspend fun retrieveContainerFileContent(
        @Path("container_id") containerId: String,
        @Path("file_id") fileId: String
    )

}

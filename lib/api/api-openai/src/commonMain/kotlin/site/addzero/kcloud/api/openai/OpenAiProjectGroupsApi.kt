// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Project groups REST endpoints. */
interface OpenAiProjectGroupsApi {

    /**
     * Lists the groups that have access to a project.
     *
     * REST: GET /organization/projects/{project_id}/groups
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_GROUPS)
    suspend fun listProjectGroups(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Grants a group access to a project.
     *
     * REST: POST /organization/projects/{project_id}/groups
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_GROUPS)
    suspend fun addProjectGroup(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Revokes a group's access to a project.
     *
     * REST: DELETE /organization/projects/{project_id}/groups/{group_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID)
    suspend fun removeProjectGroup(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String
    ): OpenAiResponseBody
}

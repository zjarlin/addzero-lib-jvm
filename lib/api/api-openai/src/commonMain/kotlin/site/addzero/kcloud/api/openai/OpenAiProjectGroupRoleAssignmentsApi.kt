// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Project group role assignments REST endpoints. */
interface OpenAiProjectGroupRoleAssignmentsApi {

    /**
     * Lists the project roles assigned to a group within a project.
     *
     * REST: GET /projects/{project_id}/groups/{group_id}/roles
     */
    @GET(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID_BY_ROLES)
    suspend fun listProjectGroupRoleAssignments(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Assigns a project role to a group within a project.
     *
     * REST: POST /projects/{project_id}/groups/{group_id}/roles
     */
    @POST(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID_BY_ROLES)
    suspend fun assignProjectGroupRole(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Unassigns a project role from a group within a project.
     *
     * REST: DELETE /projects/{project_id}/groups/{group_id}/roles/{role_id}
     */
    @DELETE(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun unassignProjectGroupRole(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String,
        @Path("role_id") roleId: String
    ): OpenAiResponseBody
}

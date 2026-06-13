// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Project user role assignments REST endpoints. */
interface OpenAiProjectUserRoleAssignmentsApi {

    /**
     * Lists the project roles assigned to a user within a project.
     *
     * REST: GET /projects/{project_id}/users/{user_id}/roles
     */
    @GET(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID_BY_ROLES)
    suspend fun listProjectUserRoleAssignments(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Assigns a project role to a user within a project.
     *
     * REST: POST /projects/{project_id}/users/{user_id}/roles
     */
    @POST(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID_BY_ROLES)
    suspend fun assignProjectUserRole(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Unassigns a project role from a user within a project.
     *
     * REST: DELETE /projects/{project_id}/users/{user_id}/roles/{role_id}
     */
    @DELETE(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun unassignProjectUserRole(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String,
        @Path("role_id") roleId: String
    ): OpenAiResponseBody
}

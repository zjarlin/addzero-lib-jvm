// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Roles REST endpoints. */
interface OpenAiRolesApi {

    /**
     * Lists the roles configured for the organization.
     *
     * REST: GET /organization/roles
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_ROLES)
    suspend fun listRoles(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Creates a custom role for the organization.
     *
     * REST: POST /organization/roles
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_ROLES)
    suspend fun createRole(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a custom role from the organization.
     *
     * REST: DELETE /organization/roles/{role_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_ROLES_BY_ROLE_ID)
    suspend fun deleteRole(
        @Path("role_id") roleId: String
    ): OpenAiResponseBody

    /**
     * Updates an existing organization role.
     *
     * REST: POST /organization/roles/{role_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_ROLES_BY_ROLE_ID)
    suspend fun updateRole(
        @Path("role_id") roleId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Lists the roles configured for a project.
     *
     * REST: GET /projects/{project_id}/roles
     */
    @GET(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES)
    suspend fun listProjectRoles(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Creates a custom role for a project.
     *
     * REST: POST /projects/{project_id}/roles
     */
    @POST(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES)
    suspend fun createProjectRole(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a custom role from a project.
     *
     * REST: DELETE /projects/{project_id}/roles/{role_id}
     */
    @DELETE(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun deleteProjectRole(
        @Path("project_id") projectId: String,
        @Path("role_id") roleId: String
    ): OpenAiResponseBody

    /**
     * Updates an existing project role.
     *
     * REST: POST /projects/{project_id}/roles/{role_id}
     */
    @POST(OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun updateProjectRole(
        @Path("project_id") projectId: String,
        @Path("role_id") roleId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}

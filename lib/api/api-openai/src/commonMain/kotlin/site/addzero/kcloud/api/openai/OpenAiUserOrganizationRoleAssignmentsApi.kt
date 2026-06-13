// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** User organization role assignments REST endpoints. */
interface OpenAiUserOrganizationRoleAssignmentsApi {

    /**
     * Lists the organization roles assigned to a user within the organization.
     *
     * REST: GET /organization/users/{user_id}/roles
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID_BY_ROLES)
    suspend fun listUserRoleAssignments(
        @Path("user_id") userId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Assigns an organization role to a user within the organization.
     *
     * REST: POST /organization/users/{user_id}/roles
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID_BY_ROLES)
    suspend fun assignUserRole(
        @Path("user_id") userId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Unassigns an organization role from a user within the organization.
     *
     * REST: DELETE /organization/users/{user_id}/roles/{role_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun unassignUserRole(
        @Path("user_id") userId: String,
        @Path("role_id") roleId: String
    ): OpenAiResponseBody
}

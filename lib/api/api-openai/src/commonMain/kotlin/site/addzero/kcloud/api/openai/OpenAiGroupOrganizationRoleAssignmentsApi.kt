// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Group organization role assignments REST endpoints. */
interface OpenAiGroupOrganizationRoleAssignmentsApi {

    /**
     * Lists the organization roles assigned to a group within the organization.
     *
     * REST: GET /organization/groups/{group_id}/roles
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_ROLES)
    suspend fun listGroupRoleAssignments(
        @Path("group_id") groupId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Assigns an organization role to a group within the organization.
     *
     * REST: POST /organization/groups/{group_id}/roles
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_ROLES)
    suspend fun assignGroupRole(
        @Path("group_id") groupId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Unassigns an organization role from a group within the organization.
     *
     * REST: DELETE /organization/groups/{group_id}/roles/{role_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun unassignGroupRole(
        @Path("group_id") groupId: String,
        @Path("role_id") roleId: String
    ): OpenAiResponseBody
}

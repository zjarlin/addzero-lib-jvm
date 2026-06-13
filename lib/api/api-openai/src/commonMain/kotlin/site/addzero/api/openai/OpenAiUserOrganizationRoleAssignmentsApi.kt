// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.DeletedRoleAssignmentResource
import site.addzero.api.openai.models.PublicAssignOrganizationGroupRoleBody
import site.addzero.api.openai.models.RoleListResource
import site.addzero.api.openai.models.UserRoleAssignment

interface OpenAiUserOrganizationRoleAssignmentsApi {

    /**
     * Lists the organization roles assigned to a user within the organization. REST: GET
     * /organization/users/{user_id}/roles
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID_BY_ROLES)
    suspend fun listUserRoleAssignments(
        @Path("user_id") userId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.RoleListResource

    /**
     * Assigns an organization role to a user within the organization. REST: POST
     * /organization/users/{user_id}/roles
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID_BY_ROLES)
    suspend fun assignUserRole(
        @Path("user_id") userId: String,
        @Body body: site.addzero.api.openai.models.PublicAssignOrganizationGroupRoleBody
    ): site.addzero.api.openai.models.UserRoleAssignment

    /**
     * Unassigns an organization role from a user within the organization. REST: DELETE
     * /organization/users/{user_id}/roles/{role_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun unassignUserRole(
        @Path("user_id") userId: String,
        @Path("role_id") roleId: String
    ): site.addzero.api.openai.models.DeletedRoleAssignmentResource

}

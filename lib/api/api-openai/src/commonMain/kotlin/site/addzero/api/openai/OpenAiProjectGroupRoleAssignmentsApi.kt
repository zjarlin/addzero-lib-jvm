// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.DeletedRoleAssignmentResource
import site.addzero.api.openai.models.GroupRoleAssignment
import site.addzero.api.openai.models.PublicAssignOrganizationGroupRoleBody
import site.addzero.api.openai.models.RoleListResource

interface OpenAiProjectGroupRoleAssignmentsApi {

    /**
     * Lists the project roles assigned to a group within a project. REST: GET
     * /projects/{project_id}/groups/{group_id}/roles
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID_BY_ROLES)
    suspend fun listProjectGroupRoleAssignments(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.RoleListResource

    /**
     * Assigns a project role to a group within a project. REST: POST
     * /projects/{project_id}/groups/{group_id}/roles
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID_BY_ROLES)
    suspend fun assignProjectGroupRole(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String,
        @Body body: site.addzero.api.openai.models.PublicAssignOrganizationGroupRoleBody
    ): site.addzero.api.openai.models.GroupRoleAssignment

    /**
     * Unassigns a project role from a group within a project. REST: DELETE
     * /projects/{project_id}/groups/{group_id}/roles/{role_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun unassignProjectGroupRole(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String,
        @Path("role_id") roleId: String
    ): site.addzero.api.openai.models.DeletedRoleAssignmentResource

}

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.PublicCreateOrganizationRoleBody
import site.addzero.api.openai.models.PublicRoleListResource
import site.addzero.api.openai.models.PublicUpdateOrganizationRoleBody
import site.addzero.api.openai.models.Role
import site.addzero.api.openai.models.RoleDeletedResource

interface OpenAiRolesApi {

    /**
     * Lists the roles configured for the organization. REST: GET /organization/roles
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ROLES)
    suspend fun listRoles(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.PublicRoleListResource

    /**
     * Creates a custom role for the organization. REST: POST /organization/roles
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ROLES)
    suspend fun createRole(
        @Body body: site.addzero.api.openai.models.PublicCreateOrganizationRoleBody
    ): site.addzero.api.openai.models.Role

    /**
     * Updates an existing organization role. REST: POST /organization/roles/{role_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ROLES_BY_ROLE_ID)
    suspend fun updateRole(
        @Path("role_id") roleId: String,
        @Body body: site.addzero.api.openai.models.PublicUpdateOrganizationRoleBody
    ): site.addzero.api.openai.models.Role

    /**
     * Deletes a custom role from the organization. REST: DELETE /organization/roles/{role_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ROLES_BY_ROLE_ID)
    suspend fun deleteRole(
        @Path("role_id") roleId: String
    ): site.addzero.api.openai.models.RoleDeletedResource

    /**
     * Lists the roles configured for a project. REST: GET /projects/{project_id}/roles
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES)
    suspend fun listProjectRoles(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.PublicRoleListResource

    /**
     * Creates a custom role for a project. REST: POST /projects/{project_id}/roles
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES)
    suspend fun createProjectRole(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.PublicCreateOrganizationRoleBody
    ): site.addzero.api.openai.models.Role

    /**
     * Updates an existing project role. REST: POST /projects/{project_id}/roles/{role_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun updateProjectRole(
        @Path("project_id") projectId: String,
        @Path("role_id") roleId: String,
        @Body body: site.addzero.api.openai.models.PublicUpdateOrganizationRoleBody
    ): site.addzero.api.openai.models.Role

    /**
     * Deletes a custom role from a project. REST: DELETE /projects/{project_id}/roles/{role_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.PROJECTS_BY_PROJECT_ID_BY_ROLES_BY_ROLE_ID)
    suspend fun deleteProjectRole(
        @Path("project_id") projectId: String,
        @Path("role_id") roleId: String
    ): site.addzero.api.openai.models.RoleDeletedResource

}

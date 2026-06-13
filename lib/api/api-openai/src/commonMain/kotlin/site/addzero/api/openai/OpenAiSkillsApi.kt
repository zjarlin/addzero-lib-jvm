// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateSkillBody
import site.addzero.api.openai.models.CreateSkillVersionBody
import site.addzero.api.openai.models.DeletedSkillResource
import site.addzero.api.openai.models.DeletedSkillVersionResource
import site.addzero.api.openai.models.OrderEnum
import site.addzero.api.openai.models.SetDefaultSkillVersionBody
import site.addzero.api.openai.models.SkillListResource
import site.addzero.api.openai.models.SkillResource
import site.addzero.api.openai.models.SkillVersionListResource
import site.addzero.api.openai.models.SkillVersionResource

interface OpenAiSkillsApi {

    /**
     * List all skills for the current project. REST: GET /skills
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS)
    suspend fun listSkills(
        @Query("limit") limit: Int? = null,
        @Query("order") order: site.addzero.api.openai.models.OrderEnum? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.SkillListResource

    /**
     * Create a new skill. REST: POST /skills
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS)
    suspend fun createSkill(
        @Body body: site.addzero.api.openai.models.CreateSkillBody? = null
    ): site.addzero.api.openai.models.SkillResource

    /**
     * Get a skill by its ID. REST: GET /skills/{skill_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID)
    suspend fun getSkill(
        @Path("skill_id") skillId: String
    ): site.addzero.api.openai.models.SkillResource

    /**
     * Update the default version pointer for a skill. REST: POST /skills/{skill_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID)
    suspend fun updateSkillDefaultVersion(
        @Path("skill_id") skillId: String,
        @Body body: site.addzero.api.openai.models.SetDefaultSkillVersionBody? = null
    ): site.addzero.api.openai.models.SkillResource

    /**
     * Delete a skill by its ID. REST: DELETE /skills/{skill_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID)
    suspend fun deleteSkill(
        @Path("skill_id") skillId: String
    ): site.addzero.api.openai.models.DeletedSkillResource

    /**
     * Download a skill zip bundle by its ID. REST: GET /skills/{skill_id}/content
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_CONTENT)
    suspend fun getSkillContent(
        @Path("skill_id") skillId: String
    ): String

    /**
     * List skill versions for a skill. REST: GET /skills/{skill_id}/versions
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS)
    suspend fun listSkillVersions(
        @Path("skill_id") skillId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: site.addzero.api.openai.models.OrderEnum? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.SkillVersionListResource

    /**
     * Create a new immutable skill version. REST: POST /skills/{skill_id}/versions
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS)
    suspend fun createSkillVersion(
        @Path("skill_id") skillId: String,
        @Body body: site.addzero.api.openai.models.CreateSkillVersionBody? = null
    ): site.addzero.api.openai.models.SkillVersionResource

    /**
     * Get a specific skill version. REST: GET /skills/{skill_id}/versions/{version}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS_BY_VERSION)
    suspend fun getSkillVersion(
        @Path("skill_id") skillId: String,
        @Path("version") version: String
    ): site.addzero.api.openai.models.SkillVersionResource

    /**
     * Delete a skill version. REST: DELETE /skills/{skill_id}/versions/{version}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS_BY_VERSION)
    suspend fun deleteSkillVersion(
        @Path("skill_id") skillId: String,
        @Path("version") version: String
    ): site.addzero.api.openai.models.DeletedSkillVersionResource

    /**
     * Download a skill version zip bundle. REST: GET /skills/{skill_id}/versions/{version}/content
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS_BY_VERSION_BY_CONTENT)
    suspend fun getSkillVersionContent(
        @Path("skill_id") skillId: String,
        @Path("version") version: String
    ): String

}

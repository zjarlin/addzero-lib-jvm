// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateModerationRequest
import site.addzero.api.openai.models.CreateModerationResponse

interface OpenAiModerationsApi {

    /**
     * Classifies if text and/or image inputs are potentially harmful. Learn more in the [moderation
     * guide](/docs/guides/moderation). REST: POST /moderations
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.MODERATIONS)
    suspend fun createModeration(
        @Body body: site.addzero.api.openai.models.CreateModerationRequest
    ): site.addzero.api.openai.models.CreateModerationResponse

}

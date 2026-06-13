// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Moderations REST endpoints. */
interface OpenAiModerationsApi {

    /**
     * Classifies if text and/or image inputs are potentially harmful. Learn more in the [moderation guide](/docs/guides/moderation).
     *
     * REST: POST /moderations
     */
    @POST(OpenAiApiPaths.MODERATIONS)
    suspend fun createModeration(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}

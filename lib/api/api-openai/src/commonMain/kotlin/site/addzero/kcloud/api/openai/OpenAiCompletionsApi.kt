// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Completions REST endpoints. */
interface OpenAiCompletionsApi {

    /**
     * Creates a completion for the provided prompt and parameters. Returns a completion object, or a sequence of completion objects if the request is streamed.
     *
     * REST: POST /completions
     */
    @POST(OpenAiApiPaths.COMPLETIONS)
    suspend fun createCompletion(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}

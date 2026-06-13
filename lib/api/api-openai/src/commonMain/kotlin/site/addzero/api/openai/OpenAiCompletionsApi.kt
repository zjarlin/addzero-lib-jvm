// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateCompletionRequest
import site.addzero.api.openai.models.CreateCompletionResponse

interface OpenAiCompletionsApi {

    /**
     * Creates a completion for the provided prompt and parameters. Returns a completion object, or a
     * sequence of completion objects if the request is streamed. REST: POST /completions
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.COMPLETIONS)
    suspend fun createCompletion(
        @Body body: site.addzero.api.openai.models.CreateCompletionRequest
    ): site.addzero.api.openai.models.CreateCompletionResponse

}

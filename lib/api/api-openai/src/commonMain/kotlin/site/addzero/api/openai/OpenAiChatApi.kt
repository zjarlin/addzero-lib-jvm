// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.ChatCompletionDeleted
import site.addzero.api.openai.models.ChatCompletionList
import site.addzero.api.openai.models.ChatCompletionMessageList
import site.addzero.api.openai.models.CreateChatCompletionRequest
import site.addzero.api.openai.models.CreateChatCompletionResponse
import site.addzero.api.openai.models.Metadata
import site.addzero.api.openai.models.UpdateChatCompletionRequest

interface OpenAiChatApi {

    /**
     * List stored Chat Completions. Only Chat Completions that have been stored with the `store` parameter
     * set to `true` will be returned. REST: GET /chat/completions
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHAT_BY_COMPLETIONS)
    suspend fun listChatCompletions(
      @Query("model") model: String? = null,
      @Query("metadata") metadata: site.addzero.api.openai.models.Metadata? = null,
      @Query("after") after: String? = null,
      @Query("limit") limit: Int? = null,
      @Query("order") order: String? = null
    ): site.addzero.api.openai.models.ChatCompletionList

    /**
     * **Starting a new project?** We recommend trying [Responses](/docs/api-reference/responses) to take
     * advantage of the latest OpenAI platform features. Compare [Chat Completions with
     * Responses](/docs/guides/responses-vs-chat-completions?api-mode=responses). --- Creates a model
     * response for the given chat conversation. Learn more in the [text generation](/docs/guides/text-
     * generation), [vision](/docs/guides/vision), and [audio](/docs/guides/audio) guides. Parameter
     * support can differ depending on the model used to generate the response, particularly for newer
     * reasoning models. Parameters that are only supported for reasoning models are noted below. For the
     * current state of unsupported parameters in reasoning models, [refer to the reasoning
     * guide](/docs/guides/reasoning). Returns a chat completion object, or a streamed sequence of chat
     * completion chunk objects if the request is streamed. REST: POST /chat/completions
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHAT_BY_COMPLETIONS)
    suspend fun createChatCompletion(
        @Body body: site.addzero.api.openai.models.CreateChatCompletionRequest
    ): site.addzero.api.openai.models.CreateChatCompletionResponse

    /**
     * Get a stored chat completion. Only Chat Completions that have been created with the `store`
     * parameter set to `true` will be returned. REST: GET /chat/completions/{completion_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHAT_BY_COMPLETIONS_BY_COMPLETION_ID)
    suspend fun getChatCompletion(
        @Path("completion_id") completionId: String
    ): site.addzero.api.openai.models.CreateChatCompletionResponse

    /**
     * Modify a stored chat completion. Only Chat Completions that have been created with the `store`
     * parameter set to `true` can be modified. Currently, the only supported modification is to update the
     * `metadata` field. REST: POST /chat/completions/{completion_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHAT_BY_COMPLETIONS_BY_COMPLETION_ID)
    suspend fun updateChatCompletion(
        @Path("completion_id") completionId: String,
        @Body body: site.addzero.api.openai.models.UpdateChatCompletionRequest
    ): site.addzero.api.openai.models.CreateChatCompletionResponse

    /**
     * Delete a stored chat completion. Only Chat Completions that have been created with the `store`
     * parameter set to `true` can be deleted. REST: DELETE /chat/completions/{completion_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHAT_BY_COMPLETIONS_BY_COMPLETION_ID)
    suspend fun deleteChatCompletion(
        @Path("completion_id") completionId: String
    ): site.addzero.api.openai.models.ChatCompletionDeleted

    /**
     * Get the messages in a stored chat completion. Only Chat Completions that have been created with the
     * `store` parameter set to `true` will be returned. REST: GET
     * /chat/completions/{completion_id}/messages
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHAT_BY_COMPLETIONS_BY_COMPLETION_ID_BY_MESSAGES)
    suspend fun getChatCompletionMessages(
        @Path("completion_id") completionId: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.ChatCompletionMessageList

}

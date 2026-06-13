// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CompactResource
import site.addzero.api.openai.models.CompactResponseMethodPublicBody
import site.addzero.api.openai.models.CreateResponse
import site.addzero.api.openai.models.IncludeEnum
import site.addzero.api.openai.models.Response
import site.addzero.api.openai.models.ResponseItemList
import site.addzero.api.openai.models.TokenCountsBody
import site.addzero.api.openai.models.TokenCountsResource

interface OpenAiResponsesApi {

    /**
     * Creates a model response. Provide [text](/docs/guides/text) or [image](/docs/guides/images) inputs
     * to generate [text](/docs/guides/text) or [JSON](/docs/guides/structured-outputs) outputs. Have the
     * model call your own [custom code](/docs/guides/function-calling) or use built-in
     * [tools](/docs/guides/tools) like [web search](/docs/guides/tools-web-search) or [file
     * search](/docs/guides/tools-file-search) to use your own data as input for the model's response.
     * REST: POST /responses
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES)
    suspend fun createResponse(
        @Body body: site.addzero.api.openai.models.CreateResponse
    ): site.addzero.api.openai.models.Response

    /**
     * Retrieves a model response with the given ID. REST: GET /responses/{response_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES_BY_RESPONSE_ID)
    suspend fun getResponse(
      @Path("response_id") responseId: String,
      @Query("include") include: List<site.addzero.api.openai.models.IncludeEnum>? = null,
      @Query("stream") stream: Boolean? = null,
      @Query("starting_after") startingAfter: Int? = null,
      @Query("include_obfuscation") includeObfuscation: Boolean? = null
    ): site.addzero.api.openai.models.Response

    /**
     * Deletes a model response with the given ID. REST: DELETE /responses/{response_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES_BY_RESPONSE_ID)
    suspend fun deleteResponse(
        @Path("response_id") responseId: String
    )

    /**
     * Cancels a model response with the given ID. Only responses created with the `background` parameter
     * set to `true` can be cancelled. [Learn more](/docs/guides/background). REST: POST
     * /responses/{response_id}/cancel
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES_BY_RESPONSE_ID_BY_CANCEL)
    suspend fun cancelResponse(
        @Path("response_id") responseId: String
    ): site.addzero.api.openai.models.Response

    /**
     * Returns a list of input items for a given response. REST: GET /responses/{response_id}/input_items
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES_BY_RESPONSE_ID_BY_INPUT_ITEMS)
    suspend fun listInputItems(
        @Path("response_id") responseId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("include") include: List<site.addzero.api.openai.models.IncludeEnum>? = null
    ): site.addzero.api.openai.models.ResponseItemList

    /**
     * Returns input token counts of the request. Returns an object with `object` set to
     * `response.input_tokens` and an `input_tokens` count. REST: POST /responses/input_tokens
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES_BY_INPUT_TOKENS)
    suspend fun getInputTokenCounts(
        @Body body: site.addzero.api.openai.models.TokenCountsBody? = null
    ): site.addzero.api.openai.models.TokenCountsResource

    /**
     * Compact a conversation. Returns a compacted response object. Learn when and how to compact long-
     * running conversations in the [conversation state guide](/docs/guides/conversation-state#managing-
     * the-context-window). For ZDR-compatible compaction details, see [Compaction
     * (advanced)](/docs/guides/conversation-state#compaction-advanced). REST: POST /responses/compact
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.RESPONSES_BY_COMPACT)
    suspend fun compactConversation(
        @Body body: site.addzero.api.openai.models.CompactResponseMethodPublicBody? = null
    ): site.addzero.api.openai.models.CompactResource

}

// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Images REST endpoints. */
interface OpenAiImagesApi {

    /**
     * Creates an edited or extended image given one or more source images and a prompt. This endpoint supports GPT Image models (`gpt-image-1.5`, `gpt-image-1`, `gpt-image-1-mini`, and `chatgpt-image-latest`) and `dall-e-2`.
     *
     * REST: POST /images/edits
     */
    @POST(OpenAiApiPaths.IMAGES_BY_EDITS)
    suspend fun createImageEdit(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Creates an image given a prompt. [Learn more](/docs/guides/images).
     *
     * REST: POST /images/generations
     */
    @POST(OpenAiApiPaths.IMAGES_BY_GENERATIONS)
    suspend fun createImage(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Creates a variation of a given image. This endpoint only supports `dall-e-2`.
     *
     * REST: POST /images/variations
     */
    @POST(OpenAiApiPaths.IMAGES_BY_VARIATIONS)
    suspend fun createImageVariation(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}

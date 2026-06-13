// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateImageRequest
import site.addzero.api.openai.models.CreateImageVariationRequest
import site.addzero.api.openai.models.EditImageBodyJsonParam
import site.addzero.api.openai.models.ImagesResponse

interface OpenAiImagesApi {

    /**
     * Creates an edited or extended image given one or more source images and a prompt. This endpoint
     * supports GPT Image models (`gpt-image-1.5`, `gpt-image-1`, `gpt-image-1-mini`, and `chatgpt-image-
     * latest`) and `dall-e-2`. REST: POST /images/edits
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.IMAGES_BY_EDITS)
    suspend fun createImageEdit(
        @Body body: site.addzero.api.openai.models.EditImageBodyJsonParam
    ): site.addzero.api.openai.models.ImagesResponse

    /**
     * Creates an image given a prompt. [Learn more](/docs/guides/images). REST: POST /images/generations
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.IMAGES_BY_GENERATIONS)
    suspend fun createImage(
        @Body body: site.addzero.api.openai.models.CreateImageRequest
    ): site.addzero.api.openai.models.ImagesResponse

    /**
     * Creates a variation of a given image. This endpoint only supports `dall-e-2`. REST: POST
     * /images/variations
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.IMAGES_BY_VARIATIONS)
    suspend fun createImageVariation(
        @Body body: site.addzero.api.openai.models.CreateImageVariationRequest
    ): site.addzero.api.openai.models.ImagesResponse

}

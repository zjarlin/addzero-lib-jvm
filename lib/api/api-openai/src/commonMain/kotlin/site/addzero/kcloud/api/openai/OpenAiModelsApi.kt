// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Models REST endpoints. */
interface OpenAiModelsApi {

    /**
     * Lists the currently available models, and provides basic information about each one such as the owner and availability.
     *
     * REST: GET /models
     */
    @GET(OpenAiApiPaths.MODELS)
    suspend fun listModels(): OpenAiResponseBody

    /**
     * Delete a fine-tuned model. You must have the Owner role in your organization to delete a model.
     *
     * REST: DELETE /models/{model}
     */
    @DELETE(OpenAiApiPaths.MODELS_BY_MODEL)
    suspend fun deleteModel(
        @Path("model") model: String
    ): OpenAiResponseBody

    /**
     * Retrieves a model instance, providing basic information about the model such as the owner and permissioning.
     *
     * REST: GET /models/{model}
     */
    @GET(OpenAiApiPaths.MODELS_BY_MODEL)
    suspend fun retrieveModel(
        @Path("model") model: String
    ): OpenAiResponseBody
}

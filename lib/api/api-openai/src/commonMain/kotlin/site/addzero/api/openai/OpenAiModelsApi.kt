// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.DeleteModelResponse
import site.addzero.api.openai.models.ListModelsResponse
import site.addzero.api.openai.models.Model

interface OpenAiModelsApi {

    /**
     * Lists the currently available models, and provides basic information about each one such as the
     * owner and availability. REST: GET /models
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.MODELS)
    suspend fun listModels(): site.addzero.api.openai.models.ListModelsResponse

    /**
     * Retrieves a model instance, providing basic information about the model such as the owner and
     * permissioning. REST: GET /models/{model}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.MODELS_BY_MODEL)
    suspend fun retrieveModel(
        @Path("model") model: String
    ): site.addzero.api.openai.models.Model

    /**
     * Delete a fine-tuned model. You must have the Owner role in your organization to delete a model.
     * REST: DELETE /models/{model}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.MODELS_BY_MODEL)
    suspend fun deleteModel(
        @Path("model") model: String
    ): site.addzero.api.openai.models.DeleteModelResponse

}

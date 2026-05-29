package site.addzero.koog

import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import org.koin.core.annotation.Single

@Single
class KoogOpenAiModelCatalog {
  fun models(): List<LLModel> {
    return OpenAIModels.models
  }

  fun chatCompletionModels(): List<LLModel> {
    return models()
      .filter { model ->
        model.supports(LLMCapability.Completion) &&
          model.supports(LLMCapability.OpenAIEndpoint.Completions)
      }
  }
}

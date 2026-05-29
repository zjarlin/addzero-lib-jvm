package site.addzero.koog

import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KoogOpenAiModelCatalogTest {
  private val catalog = KoogOpenAiModelCatalog()

  @Test
  fun returnsKoogOpenAiModelsIncludingQuickstartModel() {
    val models = catalog.models()

    assertTrue(models.isNotEmpty())
    assertTrue(models.all { model -> model.provider == LLMProvider.OpenAI })
    assertTrue(OpenAIModels.Chat.GPT4o in models)

    val gpt4o = models.single { model -> model.id == OpenAIModels.Chat.GPT4o.id }
    assertEquals("gpt-4o", gpt4o.id)
    assertTrue(gpt4o.supports(LLMCapability.Completion))
    assertTrue(gpt4o.supports(LLMCapability.OpenAIEndpoint.Completions))
  }

  @Test
  fun returnsChatCompletionModelsUsableByApplications() {
    val modelIds = catalog.chatCompletionModels().map { model -> model.id }

    assertTrue("gpt-4o" in modelIds)
    assertTrue("gpt-4o-mini" in modelIds)
    assertTrue("text-embedding-3-small" !in modelIds)
    assertTrue("omni-moderation-latest" !in modelIds)
  }
}

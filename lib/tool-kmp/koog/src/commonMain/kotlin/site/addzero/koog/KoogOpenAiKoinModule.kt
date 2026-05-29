package site.addzero.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan("site.addzero.koog")
class KoogOpenAiKoinModule {
  @Single
  fun koogOpenAiAgentConfig(provider: KoogOpenAiAgentConfigProvider): KoogOpenAiAgentConfig {
    return provider.koogOpenAiAgentConfig()
  }

  @Single
  fun openAiClientSettings(config: KoogOpenAiAgentConfig): OpenAIClientSettings {
    return OpenAIClientSettings(baseUrl = config.normalizedBaseUrl)
  }

  @Single
  fun openAiLlModel(config: KoogOpenAiAgentConfig): LLModel {
    val llModel = LLModel(
      provider = LLMProvider.OpenAI,
      id = config.modelId,
      capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.Temperature,
        LLMCapability.Tools,
        LLMCapability.OpenAIEndpoint.Completions,
      ),
    )
    return llModel
  }

  @Single
  fun openAiLlMClient(
    config: KoogOpenAiAgentConfig,
    settings: OpenAIClientSettings,
  ): LLMClient {
    val openAILLMClient = OpenAILLMClient(
      apiKey = config.apiKey,
      settings = settings,
      httpClientFactory = KtorKoogHttpClient.Factory(),
    )
    return openAILLMClient
  }

  @Single
  fun promptExecutor(openAiClient: LLMClient): PromptExecutor {
    val multiLLMPromptExecutor = MultiLLMPromptExecutor(openAiClient)
    return multiLLMPromptExecutor
  }

  @Single
  fun toolRegistry(): ToolRegistry {
    return ToolRegistry.EMPTY
  }

  @Single
  fun openAiAgent(
    promptExecutor: PromptExecutor,
    model: LLModel,
    toolRegistry: ToolRegistry,
    config: KoogOpenAiAgentConfig,
  ): AIAgent<String, String> {
    return AIAgent(
      promptExecutor = promptExecutor,
      llmModel = model,
      toolRegistry = toolRegistry,
      systemPrompt = config.systemPrompt,
      maxIterations = config.maxAgentIterations,
    )
  }
}

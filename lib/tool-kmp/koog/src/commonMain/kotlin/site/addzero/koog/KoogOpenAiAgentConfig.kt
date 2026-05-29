package site.addzero.koog

const val KOOG_OPENAI_DEFAULT_BASE_URL = "https://api.addzero.site"
const val KOOG_OPENAI_DEFAULT_MODEL_ID = "qwen2.5:7b"
const val KOOG_OPENAI_DEFAULT_SYSTEM_PROMPT = "You are an  agent. Answer clearly and keep implementation advice executable."

data class KoogOpenAiAgentConfig(
  val baseUrl: String = KOOG_OPENAI_DEFAULT_BASE_URL,
  val apiKey: String,
  val modelId: String = KOOG_OPENAI_DEFAULT_MODEL_ID,
  val systemPrompt: String = KOOG_OPENAI_DEFAULT_SYSTEM_PROMPT,
  val maxAgentIterations: Int = 20,
) {
  init {
    require(baseUrl.isNotBlank()) {
      "baseUrl must not be blank"
    }
    require(apiKey.isNotBlank()) {
      "apiKey must not be blank"
    }
    require(modelId.isNotBlank()) {
      "modelId must not be blank"
    }
    require(maxAgentIterations > 0) {
      "maxAgentIterations must be greater than 0"
    }
  }

  val normalizedBaseUrl = baseUrl.trimEnd('/')
}


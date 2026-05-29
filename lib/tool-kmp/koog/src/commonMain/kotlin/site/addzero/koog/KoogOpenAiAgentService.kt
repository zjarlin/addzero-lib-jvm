package site.addzero.koog

import ai.koog.agents.core.agent.AIAgent
import org.koin.core.annotation.Single

@Single
class KoogOpenAiAgentService(
  private val agent: AIAgent<String, String>,
) {
  suspend fun run(input: String, sessionId: String? = null): String {
    return agent.run(input, sessionId)
  }
}

package site.addzero.koog.testsupport

import org.koin.core.annotation.Single
import site.addzero.koog.KoogOpenAiAgentConfig
import site.addzero.koog.KoogOpenAiAgentConfigProvider

@Single
class KoogOpenAiConsumerTestConfigProvider : KoogOpenAiAgentConfigProvider {
  override fun koogOpenAiAgentConfig(): KoogOpenAiAgentConfig {
    return KoogOpenAiConsumerTestConfig.current()
  }
}

object KoogOpenAiConsumerTestConfig {
  private var value: KoogOpenAiAgentConfig? = null

  fun set(config: KoogOpenAiAgentConfig) {
    value = config
  }

  fun current(): KoogOpenAiAgentConfig {
    return requireNotNull(value) {
      "KoogOpenAiConsumerTestConfig must be set before creating the Koin test application."
    }
  }

  fun clear() {
    value = null
  }
}

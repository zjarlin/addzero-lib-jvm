package site.addzero.koog

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.koog.testsupport.KoogOpenAiConsumerTestConfig

@KoinApplication
object KoogOpenAiRealApiTestKoinApplication

class KoogOpenAiRealApiIntegrationTest {
  @Test
  fun canCallConfiguredAddzeroOpenAiCompatibleApiWhenEnabled() = runTest {
    val apiKey = System.getProperty("koog.openai.apiKey")
      ?: System.getenv("KOOG_OPENAI_API_KEY")
      ?: return@runTest

    KoogOpenAiConsumerTestConfig.set(
      KoogOpenAiAgentConfig(
        apiKey = apiKey,
        baseUrl = System.getProperty("koog.openai.baseUrl")
          ?: KOOG_OPENAI_DEFAULT_BASE_URL,
        modelId = System.getProperty("koog.openai.modelId")
          ?: KOOG_OPENAI_DEFAULT_MODEL_ID,
      )
    )

    val app = koinApplication {
      withConfiguration<KoogOpenAiRealApiTestKoinApplication>()
    }

    try {
      val answer = app.koin.get<KoogOpenAiAgentService>().run(
        "Reply with exactly: koog-ok",
      )

      assertTrue(answer.contains("koog-ok", ignoreCase = true), answer)
    } finally {
      app.close()
      KoogOpenAiConsumerTestConfig.clear()
    }
  }
}

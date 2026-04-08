package site.addzero.core.network

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.core.network.spi.HttpClientProfileSpi
import site.addzero.core.network.token.TokenManager

@Module
class NetworkStarterTestModule {
  @Single
  fun httpClientProfileSpi(): HttpClientProfileSpi {
    return object : HttpClientProfileSpi {
      override val baseUrl: String = "https://example.com"
    }
  }

  @Single
  fun settings(): Settings {
    return PreferencesSettings.Factory().create("network-starter-test")
  }
}

@org.koin.core.annotation.KoinApplication
object NetworkStarterTestKoinApplication

class NetworkStarterKoinModuleTest {
  @Test
  fun configurationLoadsStarterBeansAndScannedTokenManager() {
    val app = koinApplication {
      withConfiguration<NetworkStarterTestKoinApplication>()
      modules(NetworkStarterTestModule().module())
    }

    try {
      val httpClient = app.koin.get<io.ktor.client.HttpClient>()
      val tokenManager = app.koin.get<TokenManager>()

      tokenManager.clearToken()
      tokenManager.setToken("demo-token")

      assertNotNull(httpClient)
      assertEquals("demo-token", tokenManager.getToken())
    } finally {
      app.close()
    }
  }
}

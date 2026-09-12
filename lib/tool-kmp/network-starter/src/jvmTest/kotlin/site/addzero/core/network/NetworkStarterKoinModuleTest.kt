package site.addzero.core.network

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import site.addzero.core.network.spi.HttpClientProfileSpi
import site.addzero.core.network.token.TokenManager

class NetworkStarterKoinModuleTest {
  @Test
  fun explicitModuleLoadsStarterBeansAndTokenManager() {
    val app = koinApplication {
      modules(
        module {
          single<HttpClientProfileSpi> {
            object : HttpClientProfileSpi {
              override val baseUrl: String = "https://example.com"
            }
          }
          single<Settings> {
            PreferencesSettings.Factory().create("network-starter-test")
          }
        },
        networkStarterModule(),
      )
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

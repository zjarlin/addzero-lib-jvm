package site.addzero.core.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.spi.HttpClientProfileSpi

@Module
@Configuration
@ComponentScan("site.addzero.core.network")
class NetworkStarterKoinModule {

  @Single
  fun httpClient(httpClientProfileSpi: HttpClientProfileSpi): HttpClient {
    return httpClientProfileSpi.toHttpClient()
  }

  @Single
  fun ktorfit(httpClient: HttpClient): Ktorfit {
    val ktorfit = Ktorfit.Builder().httpClient(httpClient).build()
    return ktorfit
  }
}

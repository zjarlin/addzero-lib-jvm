# network-starter

Ktor `HttpClient` starter for KMP projects.

The current usage is profile-bean-first: register one `HttpClientProfileSpi`, then let the starter expose shared `HttpClient` and `Ktorfit` beans through Koin.

## What It Provides

- Public SPI: `site.addzero.core.network.spi.HttpClientProfileSpi`
- Public SPI: `site.addzero.core.network.spi.HttpResponseEventHandlerSpi`
- Koin-provided `HttpClient`
- Koin-provided `Ktorfit`
- Helpers: `HttpClient.setToken(...)` and `HttpClient.enableSSE()`

## Add Dependency

```kotlin
dependencies {
    implementation(libs.findLibrary("site-addzero-network-starter").get())
}
```

## Basic Usage

Register a single `HttpClientProfileSpi` bean. The starter uses it to build the shared client with a default base URL.

```kotlin
@Single
class DemoHttpClientProfileSpi : HttpClientProfileSpi {
    override val baseUrl: String = "https://api.example.com/v1/"
    override val enableCurlLogging: Boolean = false
}

@Module
class DemoApiModule {
    @Factory
    fun provideDemoApi(
        ktorfit: Ktorfit,
    ): DemoApi {
        return ktorfit.createDemoApi()
    }
}
```

If you prefer raw Ktor calls, inject `HttpClient` directly.

```kotlin
@Factory
fun provideDemoGateway(
    httpClient: HttpClient,
): DemoGateway {
    return DemoGateway(httpClient)
}
```

## `HttpClientProfileSpi`

`HttpClientProfileSpi` currently drives these parts of client construction:

- `baseUrl`: required; applied through `defaultRequest { url(...) }`
- `token`: optional; if null, the starter falls back to `TokenManager`
- `enableCurlLogging`: optional; defaults to `true`

```kotlin
@Single
class DemoHttpClientProfileSpi : HttpClientProfileSpi {
    override val baseUrl: String = "https://api.example.com/v1/"
    override val token: String? = "Bearer demo-token"
}
```

`headers` and `default` are still present on the SPI interface, but the current common implementation does not consume them yet. Do not rely on those two properties until the starter wires them explicitly.

## Per-Use Client Tweaks

`setToken(...)` and `enableSSE()` return derived clients from the injected base client.

```kotlin
class DemoStreamingGateway(
    private val httpClient: HttpClient,
) {
    private val authedClient = httpClient.setToken("Bearer demo-token")
    private val sseClient = authedClient.enableSSE()
}
```

`enableSSE()` installs the Ktor SSE plugin. Request-specific headers still need to be added by the caller when the upstream API requires them.

## `HttpResponseEventHandlerSpi`

`HttpResponseEventHandlerSpi` is the extension point for global response handling:

```kotlin
@Single
class DemoResponseHandler : HttpResponseEventHandlerSpi {
    override fun handle(response: HttpResponse) {
        println(response.status)
    }
}
```

At the moment, `toHttpClient()` does not install response-event dispatch automatically, so registering this SPI alone does not yet make handlers run. Keep it as a reserved extension point until the starter wires dispatch back in.

## Notes

- Platform code only selects the engine. Client construction still happens inside the starter.
- Legacy examples using `HttpClientFactory.get("profile")`, runtime profile header mutation, or profile-level SSE/WebSocket switches are outdated for the current SPI shape.

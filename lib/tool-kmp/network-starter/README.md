# network-starter

Ktor `HttpClient` shared factory for KMP projects.

## What It Provides

- Public Koin beans: `HttpClientFactory`, `ApiClients`
- Reusable named `HttpClient` profiles
- Multiple API endpoints with different `baseUrl`, optionally sharing one client profile
- Runtime header/token/feature switches
- Koin SPI for client presets, endpoint definitions, and response event handlers

## Add Dependency

```kotlin
dependencies {
    implementation(libs.findLibrary("site-addzero-network-starter").get())
}
```

## Basic Usage

```kotlin
import site.addzero.core.network.ApiClients

val apiClients = ApiClients.shared()
val client = apiClients.httpClient("my-api")
```

## Client Profile SPI

Use `HttpClientProfileSpi` when one module wants to contribute default headers, feature flags, or client config for a reusable named client.

```kotlin
@Single
class DemoClientProfile : HttpClientProfileSpi {
    override val profile: String = "demo-client"

    override fun requestContribution(): HttpClientRequestContribution {
        return HttpClientRequestContribution(
            headers = mapOf("X-Client" to "demo"),
        )
    }
}
```

## Endpoint SPI

Use `ApiEndpointSpi` when one module wants to publish one or more `baseUrl` entries. Multiple endpoints can point to the same `clientProfile`.

```kotlin
@Single
class DemoApiEndpoint : ApiEndpointSpi {
    override val endpointId: String = "demo-api"
    override val baseUrl: String = "https://api.example.com/v1"
    override val clientProfile: String = "demo-client"
}

@Single
class DemoUploadEndpoint : ApiEndpointSpi {
    override val endpointId: String = "demo-upload"
    override val baseUrl: String = "https://upload.example.com"
    override val clientProfile: String = "demo-client"
}
```

## Runtime Configuration

Runtime switches are applied by endpoint id. Endpoints that share the same `clientProfile` will share headers, token, and feature toggles.

```kotlin
val apiClients = ApiClients.shared()

apiClients.putHeader("demo-api", "X-App-Id", "demo")
apiClients.setBearerToken("demo-api", "token-value")
apiClients.setSseEnabled("demo-api", true)
apiClients.setWebSocketEnabled("demo-api", false)

val api = apiClients.create("demo-api") { baseUrl, httpClient ->
    DemoApiFactory.create(baseUrl, httpClient)
}
```

## Response Event SPI

Use `HttpResponseEventHandlerSpi` when you need to react to non-200 responses globally.

```kotlin
@Single
class DemoResponseHandler : HttpResponseEventHandlerSpi {
    override fun handle(response: HttpResponse) {
        println(response.status)
    }
}
```

## Notes

- Platform code only selects the engine. Client construction happens inside the starter.
- If a client profile's SSE or WebSocket switch changes, that cached `HttpClient` is recreated automatically.
- `ApiClients` only maps `endpointId -> baseUrl + clientProfile`; it does not duplicate `HttpClient` caches.

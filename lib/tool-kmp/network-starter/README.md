# network-starter

Ktor `HttpClient` shared factory for KMP projects.

## What It Provides

- One public entry: `HttpClientFactory`
- Profile-based client reuse
- Runtime header/token/feature switches
- Koin SPI for profile presets and response event handlers

## Add Dependency

```kotlin
dependencies {
    implementation(libs.findLibrary("site-addzero-network-starter").get())
}
```

## Basic Usage

```kotlin
import site.addzero.core.network.HttpClientFactory

val client = HttpClientFactory.shared().get("my-profile")
```

## Runtime Configuration

```kotlin
val factory = HttpClientFactory.shared()

factory.putHeader("my-profile", "X-App-Id", "demo")
factory.setBearerToken("my-profile", "token-value")
factory.setSseEnabled("my-profile", true)
factory.setWebSocketEnabled("my-profile", false)

val client = factory.get("my-profile")
```

## Profile SPI

Use `HttpClientProfileSpi` when one module wants to contribute default headers, feature flags, or client config for a profile.

```kotlin
@Single
class DemoProfile : HttpClientProfileSpi {
    override val profile: String = "demo"

    override fun requestContribution(): HttpClientRequestContribution {
        return HttpClientRequestContribution(
            headers = mapOf("X-Client" to "demo"),
        )
    }
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

- Platform code only selects the engine. Client construction happens inside the factory.
- If a profile's SSE or WebSocket switch changes, the factory recreates that profile's cached client automatically.

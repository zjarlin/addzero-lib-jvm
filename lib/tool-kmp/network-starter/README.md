# network-starter

Ktor `HttpClient` shared factory for KMP projects.

## What It Provides

- Public factory: `HttpClientFactory`
- Public SPI: `site.addzero.core.network.spi.HttpClientProfileSpi`
- Public SPI: `site.addzero.core.network.spi.HttpResponseEventHandlerSpi`
- Runtime header, token, SSE, and WebSocket switches by client profile

## Add Dependency

```kotlin
dependencies {
    implementation(libs.findLibrary("site-addzero-network-starter").get())
}
```

## Basic Usage

业务模块自己注册 API bean，不再额外包一层 endpoint registry。

```kotlin
@Module
class DemoApiModule {
    @Factory
    fun provideDemoApi(
        httpClientFactory: HttpClientFactory,
    ): DemoApi {
        return buildDemoApi(
            baseUrl = "https://api.example.com/v1/",
            httpClient = httpClientFactory.get("demo-api"),
        )
    }
}
```

## Client Profile SPI

Use `HttpClientProfileSpi` when one module wants to contribute default headers, feature flags, or client config for a reusable named client.

```kotlin
@Single
class DemoClientProfile : HttpClientProfileSpi {
    override val profile: String = "demo-api"
    override val headers: Map<String, String> = mapOf(
        "X-Client" to "demo",
    )
}
```

## Runtime Configuration

运行时开关直接作用在 client profile。

```kotlin
httpClientFactory.putHeader("demo-api", "X-App-Id", "demo")
httpClientFactory.setBearerToken("demo-api", "token-value")
httpClientFactory.setSseEnabled("demo-api", true)
httpClientFactory.setWebSocketEnabled("demo-api", false)
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
- Base URL ownership stays in the business module that provides the API bean.

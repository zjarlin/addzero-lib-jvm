# api-suno

Suno API client bindings used by KCloud.

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:api:api-suno"))
}
```

## Koin Usage

This module already exposes `SunoApiBindings`. Provide the token as a Koin property:

```kotlin
properties(
    mapOf("suno.apiToken" to "<your-token>"),
)
```

Then inject `SunoApiClient`.

## Direct Usage

```kotlin
val client = SunoApiClient(apiToken = token)
val taskId = client.generateMusic(
    SunoGenerateRequest(
        prompt = "lofi piano",
    ),
)
```

## Notes

- The HTTP client comes from `HttpClientFactory` with profile `suno-api`.
- Default request headers for this profile are contributed by `SunoHttpClientProfileSpi`.

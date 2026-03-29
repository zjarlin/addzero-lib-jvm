# config-center-client

Remote client facade for Config Center.

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:config-center:client"))
}
```

## Basic Usage

```kotlin
ConfigCenterApiClient.configureBaseUrl("http://localhost:8080/")

val gateway = ConfigCenterRemoteGateway()
val value = gateway.getEnv("demo.key", ConfigQuery())
```

## Global Shortcut

```kotlin
ConfigCenter.useRemoteGateway()
val value = ConfigCenter.getEnv("demo.key")
```

## Notes

- Requests use the shared `HttpClientFactory` profile `config-center`.
- Replace the gateway with `ConfigCenter.install(...)` if you need a custom implementation for tests or offline mode.

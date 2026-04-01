# api-netease

Netease music API bindings built on top of `network-starter`.

## What It Provides

- `MusicSearchClient` Koin bean
- `NeteaseApi` Koin factory bean
- `ApiEndpointSpi` contribution for `https://music.163.com/api/`

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:api:api-netease"))
}
```

## Usage

```kotlin
val client = KoinPlatform.getKoin().get<MusicSearchClient>()
client.mytoken = "your-token"

val songs = client.musicApi.search("稻香")
```

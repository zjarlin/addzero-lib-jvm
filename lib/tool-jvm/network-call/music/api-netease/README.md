# api-netease

Legacy Netease music client module under `tool-jvm/network-call/music`.

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:tool-jvm:network-call:music:api-netease"))
}
```

## Basic Usage

```kotlin
MusicSearchClient.mytoken = token
val api = MusicSearchClient.musicApi
```

## Notes

- This module now also uses the shared `HttpClientFactory`.
- Prefer the newer `kmp-aio/lib/api/api-netease` module when you are working inside KCloud.

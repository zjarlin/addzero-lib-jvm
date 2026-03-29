# api-qqmusic

QQ Music API models and the `QQMusic` convenience facade.

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:api:api-qqmusic"))
}
```

## Basic Usage

```kotlin
val client = HttpClientFactory.shared().get().config {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Text.Plain)
        json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Text.Html)
    }
}

val mainApi = Ktorfit.Builder()
    .baseUrl("https://u.y.qq.com/")
    .httpClient(client)
    .build()
    .createQQMusicMainApi()

val qzoneApi = Ktorfit.Builder()
    .baseUrl("https://i.y.qq.com/")
    .httpClient(client)
    .build()
    .createQQMusicQzoneApi()

val qqMusic = QQMusic(mainApi, qzoneApi)
val lyric = qqMusic.getLyric("004Z8Ihr0JIu5s")
```

## Notes

- QQ Music endpoints return mixed content types. Register both `text/plain` and `text/html` JSON converters.

# api-music-spi

Shared music API contracts for Addzero music provider integrations.

- Module path: `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/api/api-music-spi`
- Publish target: Maven Central via `addzero-lib-jvm`

Minimal usage:

```kotlin
import site.addzero.kcloud.api.music.MusicService
import site.addzero.kcloud.api.music.SearchMusicRequest
```

Runtime constraints:

- Kotlin Multiplatform module
- Intended as an SPI/contracts module for provider-specific clients

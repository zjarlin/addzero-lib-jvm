# kcloud-core

Core shared models, sync logic, and storage-related runtime pieces for KCloud.

- Module path: `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/kcloud-core`
- Publish target: Maven Central via `addzero-lib-jvm`

Minimal usage:

```kotlin
import site.addzero.kcloud.event.EventBus
import site.addzero.kcloud.model.ConflictStrategy
```

Runtime constraints:

- Kotlin Multiplatform module with JVM-specific runtime dependencies
- Still carries KCloud product-oriented package naming

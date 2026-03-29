# kbox-plugin-runtime

Runtime plugin manager and Koin integration for loading KBox plugins.

- Module path: `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/kbox-plugin-runtime`
- Publish target: Maven Central via `addzero-lib-jvm`

Minimal usage:

```kotlin
import site.addzero.kbox.runtime.KboxRuntimePluginManager
import site.addzero.kbox.runtime.KboxRuntimeKoinModule
```

Runtime constraints:

- JVM-only module
- Depends on `kbox-core` and `kbox-plugin-api`

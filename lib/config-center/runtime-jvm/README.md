# config-center-runtime-jvm

JVM runtime implementation for Config Center storage, encryption, and repository services.

- Module path: `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/config-center/runtime-jvm`
- Publish target: Maven Central via `addzero-lib-jvm`

Minimal usage:

```kotlin
import site.addzero.configcenter.runtime.ConfigCenterBootstrap
import site.addzero.configcenter.runtime.ConfigCenterRuntimeKoinModule
```

Runtime constraints:

- JVM-only module
- Depends on `config-center-spec` and `config-center-client`

# openapi-codegen

OpenAPI-based code generation processor and IR utilities.

- Module path: `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/openapi-codegen`
- Publish target: Maven Central via `addzero-lib-jvm`

Minimal usage:

```kotlin
import site.addzero.kcloud.codegen.SchemaParser
import site.addzero.kcloud.codegen.CodeEmitter
```

Runtime constraints:

- Kotlin Multiplatform/KSP-oriented module
- Still carries KCloud-oriented package naming and may later be re-homed under a generic codegen namespace

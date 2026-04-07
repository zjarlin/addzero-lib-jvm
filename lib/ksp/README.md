# KSP Modules

`lib/ksp` no longer follows a universal plugin-first rule.

- Keep a published consumer plugin only when the consumer side needs companion dependencies, extra processors, `compileOnly` injection, or other non-trivial wiring.
- If a processor is just a single artifact with no companion wiring, use raw KSP directly.
- Policy details live in [`../../docs/ksp-gradle-plugin-policy.md`](../../docs/ksp-gradle-plugin-policy.md).

## Retained Consumer Plugins

These remain the official plugin-first entrypoints:

| Plugin id | Processor artifact | Auto wiring |
| --- | --- | --- |
| `site.addzero.ksp.compose-props` | `compose-props-processor` | `compose-props-annotations` |
| `site.addzero.ksp.gen-reified` | `gen-reified-processor` | `gen-reified-core` |
| `site.addzero.ksp.ioc` | `ioc-processor` | `ioc-core` |
| `site.addzero.ksp.jimmer-entity-external` | `jimmer-entity-external-processor` | SPI subprocessors `entity2iso`, `entity2form`, `entity2mcp` |
| `site.addzero.ksp.ksp-dsl-builder` | `ksp-dsl-builder-processor` | `ksp-dsl-builder-core` |
| `site.addzero.ksp.method-semanticizer` | `method-semanticizer-processor` | `method-semanticizer-api` |
| `site.addzero.ksp.modbus-rtu` | `modbus-ksp-rtu` | `modbus-runtime` |
| `site.addzero.ksp.modbus-tcp` | `modbus-ksp-tcp` | `modbus-runtime` |
| `site.addzero.ksp.multireceiver` | `multireceiver-processor` | `kcp-multireceiver-annotations` |
| `site.addzero.ksp.singleton-adapter` | `singleton-adapter-processor` | `singleton-adapter-api` |
| `site.addzero.ksp.spring2ktor-server` | `spring2ktor-server-processor` | `spring2ktor-server-core` and `compileOnly("org.springframework:spring-web")` |
| `site.addzero.ksp.route` | `route-processor` | `route-core` |

All retained consumer plugins are implemented as precompiled script plugins under `.gradle.kts`, not `.kt implementationClass` entrypoints.

## Raw-Only Processors

These processors no longer have sibling consumer plugins:

- `jdbc2controller-processor`
- `jdbc2entity-processor`
- `jdbc2enum-processor`
- `controller2api-processor`
- `controller2feign-processor`
- `controller2iso2dataprovider-processor`
- `enum-processor`

See each processor README for the raw option keys that now replace the old typed plugin DSL.

## Raw KMP Usage

For a KMP-declared processor with no companion wiring, prefer:

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    jvm()
    sourceSets.getByName("commonMain").kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    sourceSets.getByName("jvmMain").kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
}

dependencies {
    kspCommonMainMetadata("site.addzero:controller2api-processor:VERSION")
}

ksp {
    arg("apiClientPackageName", "demo.generated.api")
    arg("apiClientAggregatorObjectName", "Apis")
    arg("apiClientAggregatorStyle", "koin")
    arg("apiClientAggregatorOutputDir", "/tmp/generated/apis")
    arg("apiClientOutputDir", "/tmp/generated/clients")
}
```

For JVM-only processors, use `ksp(...)` or `kspJvm(...)` instead of forcing a consumer plugin that adds no value.

## Non-Matrix Items

- `logger` stays as a demo/sample KSP group and is intentionally outside the official retained-plugin matrix.
- `entity2iso`, `entity2form`, and `entity2mcp` stay SPI-only.
- `lib/openapi-codegen` is tracked as a repo-scan outlier and is not part of the `lib/ksp` keep/drop migration set.

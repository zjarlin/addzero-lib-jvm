# KSP Modules

This repository now documents KSP consumption in a plugin-first form.

- Default: apply `id("site.addzero.ksp.<feature>")`
- Fallback: wire raw `ksp(...)` dependencies manually only when you need low-level control
- Cross-repo local dev: if another repo remaps `addzero-lib-jvm` modules as project paths, publish the needed `*-gradle-plugin` modules to `mavenLocal`, keep the consumer on `plugins { id("site.addzero.ksp.<feature>") }`, and avoid whole-repo `pluginManagement.includeBuild(...)`
- Policy: see [`../../docs/ksp-gradle-plugin-policy.md`](../../docs/ksp-gradle-plugin-policy.md)

## Consumer Plugins

| Plugin id | Processor artifact | Notes |
| --- | --- | --- |
| `site.addzero.ksp.jdbc2controller` | `jdbc2controller-processor` | JVM/KSP consumer plugin |
| `site.addzero.ksp.jdbc2entity` | `jdbc2entity-processor` | JVM/KSP consumer plugin |
| `site.addzero.ksp.jdbc2enum` | `jdbc2enum-processor` | Typed JDBC/dict settings extension |
| `site.addzero.ksp.logger` | `logger-processor` | Zero-config marker plugin |
| `site.addzero.ksp.compose-props` | `compose-props-processor` | Auto-adds `compose-props-annotations` |
| `site.addzero.ksp.controller2api` | `controller2api-processor` | Typed package/output config and aggregated `Apis` object |
| `site.addzero.ksp.controller2feign` | `controller2feign-processor` | Typed Feign output config |
| `site.addzero.ksp.controller2iso2dataprovider` | `controller2iso2dataprovider-processor` | Typed generated package config |
| `site.addzero.ksp.enum` | `enum-processor` | Typed enum registry output package |
| `site.addzero.ksp.gen-reified` | `gen-reified-processor` | Zero-config marker plugin |
| `site.addzero.ksp.ioc` | `ioc-processor` | Auto-adds `ioc-core` |
| `site.addzero.ksp.jimmer-entity-external` | `jimmer-entity-external-processor` | Umbrella plugin, also adds `entity2iso`, `entity2form`, `entity2mcp` SPI processors |
| `site.addzero.ksp.ksp-dsl-builder` | `ksp-dsl-builder-processor` | Zero-config marker plugin |
| `site.addzero.ksp.method-semanticizer` | `method-semanticizer-processor` | Auto-adds `method-semanticizer-api` |
| `site.addzero.ksp.modbus-rtu` | `modbus-ksp-rtu` | Recommended entry is `modbus-rtu-gradle-plugin`, auto-adds `modbus-runtime` |
| `site.addzero.ksp.modbus-tcp` | `modbus-ksp-tcp` | Recommended entry is `modbus-tcp-gradle-plugin`, auto-adds `modbus-runtime` |
| `site.addzero.ksp.multireceiver` | `multireceiver-processor` | Auto-adds `kcp-multireceiver-annotations` |
| `site.addzero.ksp.singleton-adapter` | `singleton-adapter-processor` | Auto-adds `singleton-adapter-api` |
| `site.addzero.ksp.spring2ktor-server` | `spring2ktor-server-processor` | Auto-adds `spring2ktor-server-core` and `compileOnly("org.springframework:spring-web")` |
| `site.addzero.ksp.route` | `route-processor` | Auto-adds `route-core` |

## SPI-Only Processors

The following are not standalone consumer plugins:

- `entity2iso-processor`
- `entity2form-processor`
- `entity2mcp-processor`

Consume them through `site.addzero.ksp.jimmer-entity-external`.

## Enum Registry Output

`site.addzero.ksp.enum` still works with defaults, but the generated registry package can now be overridden when needed:

```kotlin
enumProcessor {
    enumOutputPackage.set("com.example.generated.enumregistry")
}
```

## Layout

- `common/`: shared base infrastructure
- `jdbc2metadata/`: JDBC-driven generators and their consumer plugins
- `metadata/`: feature processors, umbrella processors, and consumer plugins
- `route/`: route metadata processor, runtime core, and consumer plugin

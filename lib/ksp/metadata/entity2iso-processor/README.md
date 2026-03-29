# entity2iso-processor

`entity2iso-processor` is an SPI subprocessor in the Jimmer external family.

- It is **not** a standalone direct KSP consumer entrypoint.
- Normal users should consume it through the umbrella plugin `site.addzero.ksp.jimmer-entity-external`.

## Recommended Gradle Usage

```kotlin
plugins {
    id("site.addzero.ksp.jimmer-entity-external")
}

jimmerEntityExternal {
    sharedSourceDir.set(
        layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath
    )
    entity2Iso {
        packageName.set("site.addzero.isomorphic")
        classSuffix.set("Iso")
    }
}
```

The umbrella plugin injects:

- `jimmer-entity-external-processor`
- `entity2iso-processor`
- `entity2form-processor`
- `entity2mcp-processor`

## Low-Level Fallback

Raw manual KSP wiring is still available for advanced cases, but it is not the documented default anymore. If you go that route, wire the full Jimmer external processor set yourself instead of pretending `entity2iso-processor` is an independent consumer plugin.

# jimmer-entity-external-gradle-plugin

`site.addzero.ksp.jimmer-entity-external` is the only direct consumer entry for the Jimmer external processor family.

## Recommended Usage

```kotlin
plugins {
    id("site.addzero.ksp.jimmer-entity-external")
}

jimmerEntityExternal {
    sharedSourceDir.set(layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath)
    sharedComposeSourceDir.set(layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath)
    backendServerSourceDir.set(layout.projectDirectory.dir("src/jvmMain/kotlin").asFile.absolutePath)

    entity2Iso.packageName.set("demo.generated.iso")
    entity2Iso.classSuffix.set("Iso")
    entity2Form.enabled.set(false)
    entity2Mcp.enabled.set(false)
}
```

## Child Processor Switches

- `entity2Iso.enabled`: default `true`
- `entity2Form.enabled`: default `true`
- `entity2Mcp.enabled`: default `true`

The umbrella processor always loads SPI subprocessors through `ServiceLoader`, then filters them by the serialized enable flags before `dependsOn` topological execution.

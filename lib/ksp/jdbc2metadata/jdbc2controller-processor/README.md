# Jdbc2Controller KSP Processor

`jdbc2controller-processor` is raw-KSP only. The old sibling consumer plugin is removed.

## KMP Usage

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
    kspCommonMainMetadata("site.addzero:jdbc2controller-processor:VERSION")
}

ksp {
    arg("backendServerSourceDir", "/absolute/path/to/src/commonMain/kotlin")
    arg("controllerOutPackage", "site.addzero.web.modules.controller")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `backendServerSourceDir`
- `controllerOutPackage`

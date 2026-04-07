# Enum KSP Processor

`enum-processor` is raw-KSP only. The old sibling consumer plugin is removed.

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
    kspCommonMainMetadata("site.addzero:enum-processor:VERSION")
}

ksp {
    arg("enumOutputPackage", "site.addzero.generated.enum")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `enumOutputPackage`

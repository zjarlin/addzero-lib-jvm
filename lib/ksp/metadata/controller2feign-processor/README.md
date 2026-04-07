# Controller2Feign KSP Processor

`controller2feign-processor` is now a raw-KSP processor. The old sibling consumer plugin is intentionally removed.

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
    kspCommonMainMetadata("site.addzero:controller2feign-processor:VERSION")
}

ksp {
    arg("feignOutputPackage", "com.example.generated.feign")
    arg("feignOutputDir", "/tmp/generated/feign")
    arg("feignEnabled", "true")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `feignOutputPackage`
- `feignOutputDir`
- `feignEnabled`

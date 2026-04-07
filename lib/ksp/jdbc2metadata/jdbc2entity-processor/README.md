# Jdbc2Entity KSP Processor

`jdbc2entity-processor` is raw-KSP only. The old sibling consumer plugin is removed.

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
    kspCommonMainMetadata("site.addzero:jdbc2entity-processor:VERSION")
}

ksp {
    arg("baseEntityPackage", "site.addzero.backend.entity.base")
    arg("id", "id")
    arg("createBy", "createBy")
    arg("updateBy", "updateBy")
    arg("createTime", "createTime")
    arg("updateTime", "updateTime")
    arg("backendModelSourceDir", "/absolute/path/to/src/commonMain/kotlin")
    arg("modelPackageName", "site.addzero.generated.model")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `baseEntityPackage`
- `id`
- `createBy`
- `updateBy`
- `createTime`
- `updateTime`
- `backendModelSourceDir`
- `modelPackageName`

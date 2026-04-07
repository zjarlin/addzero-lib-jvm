# Controller2Iso2DataProvider KSP Processor

`controller2iso2dataprovider-processor` is raw-KSP only. The old sibling consumer plugin is removed.

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
    kspCommonMainMetadata("site.addzero:controller2iso2dataprovider-processor:VERSION")
}

ksp {
    arg("sharedComposeSourceDir", "/absolute/path/to/src/commonMain/kotlin")
    arg("iso2DataProviderPackage", "site.addzero.generated.forms.dataprovider")
    arg("apiClientPackageName", "site.addzero.generated.api")
    arg("apiClientAggregatorObjectName", "Apis")
    arg("isomorphicPackageName", "site.addzero.generated.isomorphic")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `sharedComposeSourceDir`
- `iso2DataProviderPackage`
- `apiClientPackageName`
- `apiClientAggregatorObjectName`
- `isomorphicPackageName`

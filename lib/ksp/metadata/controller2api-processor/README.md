# Controller2Api KSP Processor

`controller2api-processor` is raw-KSP only. The old sibling consumer plugin is removed.

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
    kspCommonMainMetadata("site.addzero:controller2api-processor:VERSION")
}

ksp {
    arg("apiClientPackageName", "site.addzero.generated.api")
    arg("apiClientAggregatorObjectName", "Apis")
    arg("apiClientAggregatorStyle", "koin")
    arg("apiClientAggregatorOutputDir", "/tmp/generated/apis")
    arg("apiClientOutputDir", "/tmp/generated/clients")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `apiClientPackageName`
- `apiClientAggregatorObjectName`
- `apiClientAggregatorStyle`
- `apiClientAggregatorOutputDir`
- `apiClientOutputDir`
- `apiClientBridgePackageName`
- `apiClientBridgeOutputDir`
- `apiClientBridgeFileName`

When the three `apiClientBridge*` options are provided together, the processor generates a single public bridge entry file that contains:

- `@Module`
- `@Configuration`
- `@Single` provider methods with explicit `Ktorfit` parameters

The bridge file replaces legacy `ApisModule.kt` style output for that module and is intended to be wired into the consumer source set explicitly.

# Jdbc2Enum KSP Processor

`jdbc2enum-processor` is raw-KSP only. The old sibling consumer plugin is removed.

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
    kspCommonMainMetadata("site.addzero:jdbc2enum-processor:VERSION")
}

ksp {
    arg("enumOutputPackage", "site.addzero.generated.enums")
    arg("dictTableName", "sys_dict")
    arg("dictIdColumn", "id")
    arg("dictCodeColumn", "dict_code")
    arg("dictNameColumn", "dict_name")
    arg("dictItemTableName", "sys_dict_item")
    arg("dictItemForeignKeyColumn", "dict_id")
    arg("dictItemCodeColumn", "item_value")
    arg("dictItemNameColumn", "item_text")
    arg("sharedSourceDir", "/absolute/path/to/src/commonMain/kotlin")
    arg("jdbcDriver", "org.postgresql.Driver")
    arg("jdbcUrl", "jdbc:postgresql://localhost:5432/demo")
    arg("jdbcUsername", "postgres")
    arg("jdbcPassword", "postgres")
}
```

## Processor Options

`processorBuddy.mustMap` keys for this processor:

- `enumOutputPackage`
- `dictTableName`
- `dictIdColumn`
- `dictCodeColumn`
- `dictNameColumn`
- `dictItemTableName`
- `dictItemForeignKeyColumn`
- `dictItemCodeColumn`
- `dictItemNameColumn`
- `sharedSourceDir`

Additional raw processor options still supported by the processor implementation:

- `jdbcDriver`
- `jdbcUrl`
- `jdbcUsername`
- `jdbcPassword`
